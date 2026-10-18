//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_sketch_batch_result_rejected_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSketchBatchResultRejectedInner {
  /// Returns a new [ChronicleSketchBatchResultRejectedInner] instance.
  ChronicleSketchBatchResultRejectedInner({

    required  this.sketchId,

    required  this.reason,
  });

  @JsonKey(
    
    name: r'sketchId',
    required: true,
    includeIfNull: false,
  )


  final String sketchId;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSketchBatchResultRejectedInner &&
      other.sketchId == sketchId &&
      other.reason == reason;

    @override
    int get hashCode =>
        sketchId.hashCode +
        reason.hashCode;

  factory ChronicleSketchBatchResultRejectedInner.fromJson(Map<String, dynamic> json) => _$ChronicleSketchBatchResultRejectedInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSketchBatchResultRejectedInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

