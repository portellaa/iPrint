//
//  MainViewController.m
//  iPrint
//
//  Created by Luis Portela Afonso on 11/1/12.
//  Copyright (c) 2012 Aveiro University. All rights reserved.
//

#import "MainViewController.h"

@implementation MainViewController

@synthesize fileSelectedName = _fileSelectedName, fileSelectedLabel = _fileSelectedLabel, printFileBtn = _printFileBtn;
@synthesize files = _files;

- (void)viewDidLoad
{
    [super viewDidLoad];
	
	[_fileSelectedName setHidden:YES];
	[_fileSelectedLabel setHidden:YES];
	
	[_printFileBtn setEnabled:NO];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (void)dealloc {
    [_fileSelectedName release];
	[_fileSelectedName release];
	[_fileSelectedLabel release];
	[_printFileBtn release];
	
	[_files release];

    [super dealloc];
}

- (IBAction)selectFileClicked:(id)sender {
    
    NSArray *dirPaths;
    NSString *docsDir;
    NSFileManager *filemgr;
    
    SBTableAlert *filesBrowser;
	UIAlertView *alertNoFiles;
    
    dirPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    docsDir = [dirPaths objectAtIndex:0];
	
	NSLog(@"Documents Directory: %@", docsDir);
    
    filemgr =[NSFileManager defaultManager];
#warning change / to docsDir
    _files = [filemgr contentsOfDirectoryAtPath:@"/" error:NULL];
    
#warning only for test can be removed later
    for (int i = 0; i < [_files count]; i++)
        NSLog(@"%@", [_files objectAtIndex: i]);
	NSLog(@"Number of files: %u", [_files count]);
	
	if ([_files count] == 0) {
		alertNoFiles = [[UIAlertView alloc] initWithTitle:@"Files List" message:@"No files found." delegate:self cancelButtonTitle:@"Close" otherButtonTitles:nil, nil];
		[alertNoFiles show];
	}
	else {
		filesBrowser = [[SBTableAlert alloc] initWithTitle:@"Files List" cancelButtonTitle:@"Cancel" messageFormat:nil];

		[filesBrowser.view setTag:1];
		[filesBrowser setDelegate:self];
		[filesBrowser setDataSource:self];
		
		[filesBrowser show];
	}
    
	[_files retain];
    [filemgr release];
}


- (IBAction)printFileClicked:(id)sender {
}

#pragma mark - SBTableAlertDataSource
- (UITableViewCell *)tableAlert:(SBTableAlert *)tableAlert cellForRowAtIndexPath:(NSIndexPath *)indexPath {
	
    UITableViewCell *cell;
	
	cell = [[[SBTableAlertCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:nil] autorelease];
	[cell.textLabel setText:[_files objectAtIndex:indexPath.row]];
	
	return cell;
}

- (NSInteger)numberOfSectionsInTableAlert:(SBTableAlert *)tableAlert {
	return 1;
}

- (NSInteger)tableAlert:(SBTableAlert *)tableAlert numberOfRowsInSection:(NSInteger)section {
	return [_files count];
}

- (void)tableAlert:(SBTableAlert *)tableAlert didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
	
	NSLog(@"File Selected: %@", [_files objectAtIndex:indexPath.row]);
	
	[_fileSelectedName setText:[_files objectAtIndex:indexPath.row]];
	[_fileSelectedName setHidden:NO];
	[_fileSelectedLabel setHidden:NO];
	
	[_printFileBtn setEnabled:YES];
}

@end
