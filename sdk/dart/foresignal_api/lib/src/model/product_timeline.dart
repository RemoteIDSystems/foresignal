//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/timeline_tick.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'product_timeline.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProductTimeline {
  /// Returns a new [ProductTimeline] instance.
  ProductTimeline({

    required  this.mode,

    required  this.products,

     this.from,

     this.to,

     this.runs,

     this.nowIndex,

    required  this.ticks,

    required  this.serverTime,
  });

  @JsonKey(
    
    name: r'mode',
    required: true,
    includeIfNull: false,
  )


  final ProductTimelineModeEnum mode;



  @JsonKey(
    
    name: r'products',
    required: true,
    includeIfNull: false,
  )


  final List<String> products;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;



      /// Run used per forecast product (one run, never mixed)
  @JsonKey(
    
    name: r'runs',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? runs;



      /// Index of the tick nearest now
  @JsonKey(
    
    name: r'nowIndex',
    required: false,
    includeIfNull: false,
  )


  final int? nowIndex;



  @JsonKey(
    
    name: r'ticks',
    required: true,
    includeIfNull: false,
  )


  final List<TimelineTick> ticks;



  @JsonKey(
    
    name: r'serverTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime serverTime;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProductTimeline &&
      other.mode == mode &&
      other.products == products &&
      other.from == from &&
      other.to == to &&
      other.runs == runs &&
      other.nowIndex == nowIndex &&
      other.ticks == ticks &&
      other.serverTime == serverTime;

    @override
    int get hashCode =>
        mode.hashCode +
        products.hashCode +
        from.hashCode +
        to.hashCode +
        runs.hashCode +
        (nowIndex == null ? 0 : nowIndex.hashCode) +
        ticks.hashCode +
        serverTime.hashCode;

  factory ProductTimeline.fromJson(Map<String, dynamic> json) => _$ProductTimelineFromJson(json);

  Map<String, dynamic> toJson() => _$ProductTimelineToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ProductTimelineModeEnum {
@JsonValue(r'products')
products(r'products'),
@JsonValue(r'seamless')
seamless(r'seamless');

const ProductTimelineModeEnum(this.value);

final String value;

@override
String toString() => value;
}


