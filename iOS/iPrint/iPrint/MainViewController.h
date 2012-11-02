//
//  MainViewController.h
//  iPrint
//
//  Created by Luis Portela Afonso on 11/1/12.
//  Copyright (c) 2012 Aveiro University. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SBTableAlert.h"

@interface MainViewController : UIViewController<SBTableAlertDataSource, SBTableAlertDelegate>

@property(retain) NSArray *files;

@property (retain, nonatomic) IBOutlet UILabel *fileSelectedName;
@property (retain, nonatomic) IBOutlet UILabel *fileSelectedLabel;
@property (retain, nonatomic) IBOutlet UIButton *printFileBtn;

- (IBAction)selectFileClicked:(id)sender;
- (IBAction)printFileClicked:(id)sender;

@end
