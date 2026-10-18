//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'propagation_graph_cross_platform_order_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PropagationGraphCrossPlatformOrderInner {
  /// Returns a new [PropagationGraphCrossPlatformOrderInner] instance.
  PropagationGraphCrossPlatformOrderInner({

    required  this.platform,

    required  this.firstObservedAt,
  });

      /// Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the platform an EDP provider (024, 138) declares
  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final String platform;



  @JsonKey(
    
    name: r'firstObservedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime firstObservedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PropagationGraphCrossPlatformOrderInner &&
      other.platform == platform &&
      other.firstObservedAt == firstObservedAt;

    @override
    int get hashCode =>
        platform.hashCode +
        firstObservedAt.hashCode;

  factory PropagationGraphCrossPlatformOrderInner.fromJson(Map<String, dynamic> json) => _$PropagationGraphCrossPlatformOrderInnerFromJson(json);

  Map<String, dynamic> toJson() => _$PropagationGraphCrossPlatformOrderInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

