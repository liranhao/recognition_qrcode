//
//  ImageViewController.m
//  recognition_qrcode
//
//  Created by 李然豪 on 2023/7/23.
//

#import "ImageViewController.h"
#import "RecognitionConfig.h"
@interface ImageViewController ()
{
    UIImageView *_imgView; //展示视图
}
@end

@implementation ImageViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self initView];
    
    // Do any additional setup after loading the view.
}
- (void)initView{
    _imgView = [[UIImageView alloc] init];
    self.view.backgroundColor = UIColor.blackColor;
    
    [self.view addSubview:_imgView];
    _imgView.image = self.image;
    
    _imgView.frame = self.view.frame;
    _imgView.contentMode = UIViewContentModeScaleAspectFit;
    
    UIView *maskView = [[UIView alloc] initWithFrame:self.view.bounds];
    maskView.backgroundColor = UIColor.blackColor;
    maskView.alpha = 0.5;
    [self.view addSubview:maskView];
    //计算image相对于ImageView的位置
    CGRect imgRect = [self calculateClientRectOfImageInUIImageView:_imgView];
    RecognitionConfig *config = RecognitionConfig.shareInstance;
    for(int i = 0; i < _barcodes.count; i ++){
        BarCodeObject *barcode = [_barcodes objectAtIndex:i];
        CGRect barcodeFrame = [self calculateBarcodeRect:imgRect barcodeRect:barcode.bounds];
        UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
        //计算二维码位置
        btn.frame = CGRectMake(barcodeFrame.origin.x + barcodeFrame.size.width / 2 - config.iconWidth / 2, barcodeFrame.origin.y + barcodeFrame.size.height / 2 -  config.iconHeight / 2, config.iconWidth, config.iconHeight);
        
        if(config.icon){
            [btn setImage:[UIImage imageWithData:config.icon.data] forState:UIControlStateNormal];
        } else {
            btn.layer.cornerRadius = config.iconHeight / 2;
            btn.layer.masksToBounds = true;
            [btn setImage:[UIImage imageNamed:@"bx-right-arrow"] forState:UIControlStateNormal];
            btn.backgroundColor = UIColor.whiteColor;
        }
        btn.imageView.contentMode = UIViewContentModeScaleAspectFit;
        btn.tag = 100 + i;
        [btn addTarget:self action:@selector(clickBarCode:) forControlEvents:UIControlEventTouchUpInside];
        [self.view addSubview:btn];
    }
    
    //关闭按钮
    UIButton *closeBtn = [[UIButton alloc] init];
    CGFloat height = 0.0;//最终高度存储容器
    if (@available(iOS 13.0, *)) {
        CGFloat topHeight = [UIApplication sharedApplication].windows.firstObject.safeAreaInsets.top;
        height = topHeight ? topHeight : 20.0;
    }else {
        height = [[UIApplication sharedApplication] statusBarFrame].size.height;
    }
    closeBtn.frame = CGRectMake(20, height, 0, 40);
    if(config.cancelTitle){
        [closeBtn setTitle:config.cancelTitle forState: UIControlStateNormal];
    } else {
        [closeBtn setTitle:@"取消" forState: UIControlStateNormal];
    }
    closeBtn.titleLabel.font = [UIFont systemFontOfSize:config.cancelTitleFontSize];
    [closeBtn sizeToFit];
    [closeBtn setTitleColor:UIColor.whiteColor forState:UIControlStateNormal];
    [closeBtn addTarget:self action:@selector(btnClose) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:closeBtn];
}
- (void) clickBarCode:(UIButton *)btn{
    BarCodeObject *barcode = [_barcodes objectAtIndex:btn.tag - 100];
    self.clickBarCodeFinish(barcode.value);
    [self dismissViewControllerAnimated:true completion:nil];
}

- (void)btnClose{
    [self dismissViewControllerAnimated:true completion:nil];
}
-(CGRect )calculateClientRectOfImageInUIImageView:(UIImageView *)imgView
{
    CGSize imgViewSize=imgView.frame.size;                  // Size of UIImageView
    CGSize imgSize=imgView.image.size;                      // Size of the image, currently displayed

    // Calculate the aspect, assuming imgView.contentMode==UIViewContentModeScaleAspectFit

    CGFloat scaleW = imgViewSize.width / imgSize.width;
    CGFloat scaleH = imgViewSize.height / imgSize.height;
    CGFloat aspect=fmin(scaleW, scaleH);

    CGRect imageRect={ {0,0} , { imgSize.width*=aspect, imgSize.height*=aspect } };

    imageRect.origin.x=(imgViewSize.width-imageRect.size.width)/2;
    imageRect.origin.y=(imgViewSize.height-imageRect.size.height)/2;

    // Add imageView offset

    imageRect.origin.x+=imgView.frame.origin.x;
    imageRect.origin.y+=imgView.frame.origin.y;

    return(imageRect);
}
-(CGRect )calculateBarcodeRect:(CGRect )imgRect barcodeRect:(CGRect) barRect{
    // Size of the image, currently displayed

    // Calculate the aspect, assuming imgView.contentMode==UIViewContentModeScaleAspectFit
    CGSize imgSize = _image.size;                  // Size of UIImageView
    CGSize barcodeSize = barRect.size;
    CGPoint barcodePoint = barRect.origin;
   

    CGFloat scaleW = _imgView.frame.size.width / imgSize.width;
    CGFloat scaleH = _imgView.frame.size.height / imgSize.height;
    CGFloat aspect=fmin(scaleW, scaleH);
    CGRect barcodeRect= CGRectMake(0, 0, 0, 0);

    barcodeRect.origin.x= barcodePoint.x * aspect + imgRect.origin.x;
    barcodeRect.origin.y= barcodePoint.y * aspect + imgRect.origin.y;
    
    barcodeRect.size.width = barcodeSize.width * aspect;
    barcodeRect.size.height = barcodeSize.height * aspect;
    return(barcodeRect);
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
