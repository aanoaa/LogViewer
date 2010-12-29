//
//  RootViewController.h
//  IRCLogViewer
//
//  Created by Yuni Kunho Kim on 12/27/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface RootViewController : UITableViewController {
	NSString *currentTitle;
	NSString *channel;
	NSString *year;
	NSString *month;
	NSString *date;
	NSString *epoch;
	
	NSArray *list;
}

@property (nonatomic, retain) NSString *channel;
@property (nonatomic, retain) NSString *year;
@property (nonatomic, retain) NSString *month;
@property (nonatomic, retain) NSString *date;
@property (nonatomic, retain) NSString *epoch;
@property (nonatomic, retain) NSArray *list;

-(NSString *)stringTime:(NSString *)timestamp;
-(NSString *)stringDateTime:(NSString *)timestamp;
-(void)setUILabelTextWithVerticalAlignTop:(NSString *)theText label:(UILabel*)label labelSize:(CGSize)labelSize;
-(void)initData;
- (CGFloat)heightForRowAtIndexPath:(NSIndexPath *)indexPath;

@end
