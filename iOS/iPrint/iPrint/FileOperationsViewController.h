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

typedef enum
{
	INITIAL = 0,
	INFO = 1,
	DATA = 2,
	END = 3
} SOCKETSTAT;

enum
{
	START_FILE_INFO,
	END_FILE_INFO,
	START_FILE_DATA,
	END_FILE_DATA
};

@interface FileOperationsViewController : UIViewController <NSStreamDelegate, QLPreviewControllerDataSource>
{
	NSInputStream *inputStream;
	NSOutputStream *outputStream;
	
	SOCKETSTAT socketStatus;
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
