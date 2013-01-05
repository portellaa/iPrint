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
	
	MBProgressHUD *printAnimation = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
	[printAnimation setMode:MBProgressHUDModeIndeterminate];
	
	[printAnimation setLabelText:@"Printing..."];
	
	NSData *fileToSendData;

	CFReadStreamRef readStream;
    CFWriteStreamRef writeStream;
    CFStreamCreatePairWithSocketToHost(NULL, (CFStringRef)@"localhost", 6969, &readStream, &writeStream);
    inputStream = (NSInputStream *)readStream;
    outputStream = (NSOutputStream *)writeStream;
	
	[inputStream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
	[outputStream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
	
	[inputStream open];
	[outputStream open];
	
	[inputStream setDelegate: self];
	[outputStream setDelegate: self];
	
	fileToSendData = [[NSFileManager defaultManager] contentsAtPath: [NSString stringWithFormat:@"%@/%@", _docsDir, _filePath]];
	
	[outputStream write: [fileToSendData bytes] maxLength: [fileToSendData length]];
}

- (void)stream:(NSStream *)aStream handleEvent:(NSStreamEvent)eventCode
{
	NSLog(@"Received stream event %i.", eventCode);
	
	switch (eventCode) {
		case NSStreamEventHasBytesAvailable:
			
			NSLog(@"Data available on socket");
			
			if (inputStream == aStream)
			{
				uint8_t buffer[1024];
				int len;
				
				while ([inputStream hasBytesAvailable])
				{
					len = [inputStream read:buffer maxLength:sizeof(buffer)];
					
					if (len > 0) {
						NSString *output = [[NSString alloc] initWithBytes:buffer length:len encoding:NSASCIIStringEncoding];
						
						if (output != nil)
						{
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
						}
					}
				}
			}
			
			break;
			
		default:
			break;
	}
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
