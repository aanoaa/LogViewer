//
//  DetailViewController.h
//  IRCLogViewer
//
//  Created by Yuni Kunho Kim on 12/29/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IFTweetLabel.h"

@interface DetailViewController : UIViewController {
	IBOutlet UILabel *nickLabel;
	IBOutlet UILabel *datetimeLabel;
	IBOutlet IFTweetLabel *talkLabel;
	
	NSString *nick;
	NSString *datetime;
	NSString *talk;
}

@property (nonatomic, retain) UILabel *nickLabel;
@property (nonatomic, retain) UILabel *datetimeLabel;
@property (nonatomic, retain) IFTweetLabel *talkLabel;

@property (nonatomic, retain) NSString *nick;
@property (nonatomic, retain) NSString *datetime;
@property (nonatomic, retain) NSString *talk;


@end
