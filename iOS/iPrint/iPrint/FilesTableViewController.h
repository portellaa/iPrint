//
//  FilesTableViewController.h
//  iPrint
//
//  Created by Luis Portela Afonso on 1/3/13.
//  Copyright (c) 2013 Aveiro University. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FileOperationsViewController.h"

@interface FilesTableViewController : UITableViewController

@property (retain, nonatomic) NSArray *files;
@property (strong) NSString *docsDir;

@end
