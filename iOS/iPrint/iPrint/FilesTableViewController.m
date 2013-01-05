//
//  FilesTableViewController.m
//  iPrint
//
//  Created by Luis Portela Afonso on 1/3/13.
//  Copyright (c) 2013 Aveiro University. All rights reserved.
//

#import "FilesTableViewController.h"

@interface FilesTableViewController ()

@end

@implementation FilesTableViewController

@synthesize files = _files;
@synthesize docsDir = _docsDir;

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        self.title = NSLocalizedString(@"Files", @"Files");
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	
	NSLog(@"Called viewDidLoad");
	
	self.title = @"Files";
	
    NSFileManager *filemgr;
	
	UIAlertView *alertNoFiles;
	
	_docsDir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
	
	NSLog(@"Documents Directory: %@", _docsDir);
    
    filemgr =[NSFileManager defaultManager];
    _files = [filemgr contentsOfDirectoryAtPath:_docsDir error:NULL];
    
	NSLog(@"Number of files: %u", [_files count]);
	
	if ([_files count] == 0) {
		alertNoFiles = [[UIAlertView alloc] initWithTitle:@"Files List" message:@"No files found." delegate:self cancelButtonTitle:@"Close" otherButtonTitles:nil, nil];
		[alertNoFiles show];
	}
    
	[_files retain];
	[_docsDir retain];
    [filemgr release];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [_files count] > 0 ? 1 : 0;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
#warning Incomplete method implementation.
    // Return the number of rows in the section.
    return [_files count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"FileCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier forIndexPath:indexPath];

	[cell.textLabel setText: [_files objectAtIndex:[indexPath row]]];
    
    return cell;
}

/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSLog(@"Selected row %d", indexPath.row);
    // Navigation logic may go here. Create and push another view controller.
    /*
     <#DetailViewController#> *detailViewController = [[<#DetailViewController#> alloc] initWithNibName:@"<#Nib name#>" bundle:nil];
     // ...
     // Pass the selected object to the new view controller.
     [self.navigationController pushViewController:detailViewController animated:YES];
     [detailViewController release];
     */
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{

	NSLog(@"Launching File Operation View.\n");
	NSLog(@"DocsDir: %@\n", _docsDir);
	
	FileOperationsViewController *fileOpVC = [segue destinationViewController];
	
	NSIndexPath *indexPath = [self.tableView indexPathForSelectedRow];
	fileOpVC.filePath = [_files objectAtIndex:[indexPath row]];
	fileOpVC.docsDir = _docsDir;
}

@end
