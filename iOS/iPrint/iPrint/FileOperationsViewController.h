//
//  FileOperationsViewController.h
//  iPrint
//
//  Created by Luis Portela Afonso on 1/3/13.
//  Copyright (c) 2013 Aveiro University. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <QuickLook/QLPreviewController.h>
#import "MBProgressHUD.h"
#import "PreviewViewController.h"

@interface FileOperationsViewController : UIViewController <NSStreamDelegate, QLPreviewControllerDataSource>
{
	NSInputStream *inputStream;
	NSOutputStream *outputStream;
}

@property (strong) NSString *filePath;
@property (strong) NSString *docsDir;

- (IBAction)printFileClicked:(id)sender;
- (IBAction)previewFileClicked:(id)sender;


@property (retain, nonatomic) IBOutlet UILabel *filenameLabel;
@property (retain, nonatomic) IBOutlet UILabel *filetypeLabel;
@property (retain, nonatomic) IBOutlet UILabel *filesizeLabel;
@property (retain, nonatomic) IBOutlet UILabel *creationdateLabel;

- (void) showAlertWithTitle:(NSString*) title andMessage:(NSString*) message;

@end
