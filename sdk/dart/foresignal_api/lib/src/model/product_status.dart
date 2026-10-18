//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/health_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'product_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProductStatus {
  /// Returns a new [ProductStatus] instance.
  ProductStatus({

    required  this.health,

    required  this.stale,

     this.lastFrameAt,

     this.reason,
  });

  @JsonKey(
    
    name: r'health',
    required: true,
    includeIfNull: false,
  )


  final HealthStatus health;



      /// Newest frame older than `maxGap`
  @JsonKey(
    
    name: r'stale',
    required: true,
    includeIfNull: false,
  )


  final bool stale;



  @JsonKey(
    
    name: r'lastFrameAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastFrameAt;



      /// Health reason, e.g. a silent format change or a disabled decoder
  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProductStatus &&
      other.health == health &&
      other.stale == stale &&
      other.lastFrameAt == lastFrameAt &&
      other.reason == reason;

    @override
    int get hashCode =>
        health.hashCode +
        stale.hashCode +
        (lastFrameAt == null ? 0 : lastFrameAt.hashCode) +
        (reason == null ? 0 : reason.hashCode);

  factory ProductStatus.fromJson(Map<String, dynamic> json) => _$ProductStatusFromJson(json);

  Map<String, dynamic> toJson() => _$ProductStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

