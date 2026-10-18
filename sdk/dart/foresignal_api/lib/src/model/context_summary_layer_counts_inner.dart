//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'context_summary_layer_counts_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ContextSummaryLayerCountsInner {
  /// Returns a new [ContextSummaryLayerCountsInner] instance.
  ContextSummaryLayerCountsInner({

    required  this.layer,

    required  this.count,

     this.liveCount,
  });

  @JsonKey(
    
    name: r'layer',
    required: true,
    includeIfNull: false,
  )


  final String layer;



  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;



  @JsonKey(
    
    name: r'liveCount',
    required: false,
    includeIfNull: false,
  )


  final int? liveCount;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ContextSummaryLayerCountsInner &&
      other.layer == layer &&
      other.count == count &&
      other.liveCount == liveCount;

    @override
    int get hashCode =>
        layer.hashCode +
        count.hashCode +
        (liveCount == null ? 0 : liveCount.hashCode);

  factory ContextSummaryLayerCountsInner.fromJson(Map<String, dynamic> json) => _$ContextSummaryLayerCountsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ContextSummaryLayerCountsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

