//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'product_temporal_retention_ladder_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProductTemporalRetentionLadderInner {
  /// Returns a new [ProductTemporalRetentionLadderInner] instance.
  ProductTemporalRetentionLadderInner({

    required  this.olderThan,

    required  this.keepEvery,
  });

      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'olderThan',
    required: true,
    includeIfNull: false,
  )


  final String olderThan;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'keepEvery',
    required: true,
    includeIfNull: false,
  )


  final String keepEvery;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProductTemporalRetentionLadderInner &&
      other.olderThan == olderThan &&
      other.keepEvery == keepEvery;

    @override
    int get hashCode =>
        olderThan.hashCode +
        keepEvery.hashCode;

  factory ProductTemporalRetentionLadderInner.fromJson(Map<String, dynamic> json) => _$ProductTemporalRetentionLadderInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ProductTemporalRetentionLadderInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

