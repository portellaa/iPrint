//
//  FileOperationsViewController.m
//  iPrint
//
//  Created by Luis Portela Afonso on 1/3/13.
//  Copyright (c) 2013 Aveiro University. All rights reserved.
//

#import "FileOperationsViewController.h"

@interface FileOperationsViewController ()

@end

@implementation FileOperationsViewController

@synthesize filenameLabel = _filenameLabel;
@synthesize filesizeLabel = _filesizeLabel;
@synthesize filetypeLabel = _filetypeLabel;
@synthesize creationdateLabel = _creationdateLabel;
@synthesize filePath = _filePath;
@synthesize docsDir = _docsDir;

- (void)viewDidLoad
{
    [super viewDidLoad];
	
	socketStatus = INITIAL;
	
	NSFileManager *filemgr;
	NSDictionary *fileAttributes;
	NSDateFormatter *dateFormatter = nil;
	
	[_filenameLabel setText:_filePath];
	
	NSLog(@"File Path received: %@/%@", _docsDir, _filePath);
	
	filemgr = [NSFileManager defaultManager];
	fileAttributes = [filemgr attributesOfItemAtPath:[NSString stringWithFormat:@"%@/%@", _docsDir, _filePath ] error:nil];
	
	for (int i = 0; i < [fileAttributes count]; i++) {
		NSLog(@"Key: %@ - Value: %@", [[fileAttributes allKeys] objectAtIndex:i], [[fileAttributes allValues] objectAtIndex:i]);
	}
	
	NSLog(@"File Type: %@", [fileAttributes fileType]);
	
	[_filetypeLabel setText: [NSString stringWithFormat:@"%@", [fileAttributes fileType]]];
	[_filesizeLabel setText: [NSString stringWithFormat:@"%llu bytes", [fileAttributes fileSize]]];
	
	dateFormatter = [[[NSDateFormatter alloc] init] autorelease];
	[dateFormatter setDateStyle:NSDateFormatterMediumStyle];
	[dateFormatter setTimeStyle:NSDateFormatterShortStyle];
	[dateFormatter setLocale: [NSLocale currentLocale]];
	
	[_creationdateLabel setText: [dateFormatter stringFromDate: [fileAttributes fileCreationDate]]];
	
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (IBAction)printFileClicked:(id)sender {
	
	NSLog(@"Starting sending file to server...");
	
	socketStatus = INITIAL;
	
	[self sendFileToServer];
}

- (IBAction)previewFileClicked:(id)sender {
	
	// Creates new QLViewController
	PreviewViewController *preview = [[[PreviewViewController alloc] init] autorelease];
	
	[preview setDataSource:self];
	// Which item to preview
	[preview setCurrentPreviewItemIndex:0];
	
	// Push new viewcontroller, previewing the document
	[[self navigationController] pushViewController:preview animated:YES];
	
}

- (NSInteger) numberOfPreviewItemsInPreviewController: (QLPreviewController *) controller
{
	return 1;
}

- (id <QLPreviewItem>)previewController: (QLPreviewController *)controller previewItemAtIndex:(NSInteger)index
{
	return [NSURL fileURLWithPath:[NSString stringWithFormat:@"%@/%@", _docsDir, _filePath]];
}

- (void) sendFileToServer
{
	CFReadStreamRef readStream;
    CFWriteStreamRef writeStream;
    CFStreamCreatePairWithSocketToHost(NULL, (CFStringRef)@"localhost", 6969, &readStream, &writeStream);
    inputStream = (NSInputStream *)readStream;
    outputStream = (NSOutputStream *)writeStream;
	
	[inputStream setDelegate: self];
	[outputStream setDelegate: self];
	
	[inputStream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
	[outputStream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
	
	[inputStream open];
	[outputStream open];
	
	NSData *message = [[NSString stringWithUTF8String:"START_FILE_INFO"] dataUsingEncoding:NSUTF8StringEncoding];

	[outputStream write: [message bytes] maxLength: [message length]];
}

- (void)stream:(NSStream *)aStream handleEvent:(NSStreamEvent)eventCode
{
	NSLog(@"Received stream event %i.", eventCode);
	
	switch (eventCode) {
			
		case NSStreamEventNone:
		{
			NSLog(@"Event None");
			[self showAlertWithTitle:@"Socket" andMessage:@"Could not connect to server."];
			
			break;
		}
		case NSStreamEventHasBytesAvailable:
			
			NSLog(@"Data available on socket");
			
			if (inputStream == aStream)
			{
				switch (socketStatus) {
					case INITIAL: {
						NSLog(@"INITIAL Status");
						if ([self readAckFromInputStrem:inputStream] == 1)
						{
							MBProgressHUD *printAnimation = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
							[printAnimation setMode:MBProgressHUDModeIndeterminate];
							
							[printAnimation setLabelText:@"Printing..."];
							
							NSDictionary *fileAttributes = [[NSFileManager defaultManager] attributesOfItemAtPath:[NSString stringWithFormat:@"%@/%@", _docsDir, _filePath ] error:nil];
							NSData *message = [[NSString stringWithFormat:@"E:%@-S:%llu", [[_filePath componentsSeparatedByString:@"."] objectAtIndex:1], [fileAttributes fileSize]] dataUsingEncoding:NSUTF8StringEncoding];
							[outputStream write: [message bytes] maxLength: [message length]];
							socketStatus = INFO;
						}
						break;
					}
					case INFO: {
						NSLog(@"INFO Status");
						if ([self readAckFromInputStrem:inputStream] == 1)
						{
							NSData *message = [[NSString stringWithUTF8String:"END_FILE_INFO"] dataUsingEncoding:NSUTF8StringEncoding];
							[outputStream write: [message bytes] maxLength: [message length]];
							
							socketStatus = DATA;
						}
						break;
					}
					case DATA: {
						NSLog(@"DATA Status");
						if ([self readAckFromInputStrem:inputStream] == 1)
						{
							NSData *fileToSendData = [[NSFileManager defaultManager] contentsAtPath: [NSString stringWithFormat:@"%@/%@", _docsDir, _filePath]];
							[outputStream write: [fileToSendData bytes] maxLength: [fileToSendData length]];
							
							socketStatus = END;
						}
						break;
					}
					case END: {
						NSLog(@"END Status");
						NSString *output = [self readMessageFromInputStrem:inputStream];
						
						NSLog(@"Server answer: %@", output);
						
						if ([output isEqualToString:@"DATA_SENT_OK"])
						{
							[MBProgressHUD hideHUDForView:self.view animated:YES];
							[self showAlertWithTitle:@"Print File" andMessage:@"File Printed"];
						}
						else if ([output isEqualToString:@"DATA_SENT_ERROR"])
						{
							[self showAlertWithTitle:@"Print File" andMessage:@"File not printed."];
						}
						
						[inputStream setDelegate:nil];
						[outputStream setDelegate:nil];
						
						[inputStream close];
						[outputStream close];
						
						break;
					}
				}
			}
			
			break;
			
		case NSStreamEventErrorOccurred: {
			
			NSLog(@"Error Occurred");
			
			[inputStream close];
			[inputStream removeFromRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
			[inputStream release];
			
			[outputStream close];
			[outputStream removeFromRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
			[outputStream release];

			if (socketStatus == INITIAL)
				[self showAlertWithTitle:@"Socket" andMessage:@"Error openning socket."];
			else if (socketStatus == END)
				[self showAlertWithTitle:@"Print" andMessage:@"Error printing file."];
			else
				[self showAlertWithTitle:@"Socket" andMessage:@"Error sending information to server."];
			
			break;
		}
			
		case NSStreamEventEndEncountered:
			
			[aStream close];
			[aStream removeFromRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
			[aStream release];
			aStream = nil;
			
			break;
			
		default:
			break;
	}
}

- (int) readAckFromInputStrem:(NSInputStream*)input
{
	NSLog(@"Read ack fom input");
	uint8_t buffer[1];
	
	[input read:buffer maxLength:sizeof(buffer)];
	
	return (int)buffer[0];
}

- (NSString*) readMessageFromInputStrem:(NSInputStream*)input
{
	uint8_t buffer[1024];
	int len;
	NSString* output;
	
	while ([input hasBytesAvailable])
	{
		len = [input read:buffer maxLength:sizeof(buffer)];
		
		if (len > 0) {
			output = [[NSString alloc] initWithBytes:buffer length:len encoding:NSASCIIStringEncoding];
		}
	}
	
	return output;
}

- (void) showAlertWithTitle:(NSString*) title andMessage:(NSString*) message
{
	UIAlertView *alert = [[UIAlertView alloc] initWithTitle:title message:message delegate:nil cancelButtonTitle:@"OK" otherButtonTitles: nil];
	[alert show];
	[alert release];
}

- (void)dealloc {
	[_filenameLabel release];
	[_filetypeLabel release];
	[_filesizeLabel release];
	[_creationdateLabel release];
	[super dealloc];
}
@end
