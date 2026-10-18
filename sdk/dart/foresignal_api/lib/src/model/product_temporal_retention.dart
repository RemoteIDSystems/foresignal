//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/product_temporal_retention_ladder_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'product_temporal_retention.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProductTemporalRetention {
  /// Returns a new [ProductTemporalRetention] instance.
  ProductTemporalRetention({

     this.ladder,

     this.maxAge,

     this.runs,

     this.summary,
  });

      /// Thinning rungs, ascending
  @JsonKey(
    
    name: r'ladder',
    required: false,
    includeIfNull: false,
  )


  final List<ProductTemporalRetentionLadderInner>? ladder;



      /// Frames older than this are deleted (observation products); null keeps them until the disk quota evicts them. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'maxAge',
    required: false,
    includeIfNull: false,
  )


  final String? maxAge;



      /// Complete forecast runs kept (forecasts: 2)
  @JsonKey(
    
    name: r'runs',
    required: false,
    includeIfNull: false,
  )


  final int? runs;



      /// One-line description
  @JsonKey(
    
    name: r'summary',
    required: false,
    includeIfNull: false,
  )


  final String? summary;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProductTemporalRetention &&
      other.ladder == ladder &&
      other.maxAge == maxAge &&
      other.runs == runs &&
      other.summary == summary;

    @override
    int get hashCode =>
        ladder.hashCode +
        (maxAge == null ? 0 : maxAge.hashCode) +
        (runs == null ? 0 : runs.hashCode) +
        summary.hashCode;

  factory ProductTemporalRetention.fromJson(Map<String, dynamic> json) => _$ProductTemporalRetentionFromJson(json);

  Map<String, dynamic> toJson() => _$ProductTemporalRetentionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

