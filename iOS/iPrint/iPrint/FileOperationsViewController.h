//
//  FileOperationsViewController.h
//  iPrint
//
//  Created by Luis Portela Afonso on 1/3/13.
//  Copyright (c) 2013 Aveiro University. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface FileOperationsViewController : UIViewController <NSStreamDelegate>
{
	NSInputStream *inputStream;
	NSOutputStream *outputStream;
}

@property (strong) NSString *filePath;
@property (strong) NSString *docsDir;

- (IBAction)printFileClicked:(id)sender;

@property (retain, nonatomic) IBOutlet UILabel *filenameLabel;
@property (retain, nonatomic) IBOutlet UILabel *filetypeLabel;
@property (retain, nonatomic) IBOutlet UILabel *filesizeLabel;
@property (retain, nonatomic) IBOutlet UILabel *creationdateLabel;


@end
