//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_sketch_batch_result_rejected_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_sketch_batch_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSketchBatchResult {
  /// Returns a new [ChronicleSketchBatchResult] instance.
  ChronicleSketchBatchResult({

    required  this.accepted,

    required  this.rejected,
  });

  @JsonKey(
    
    name: r'accepted',
    required: true,
    includeIfNull: false,
  )


  final int accepted;



  @JsonKey(
    
    name: r'rejected',
    required: true,
    includeIfNull: false,
  )


  final List<ChronicleSketchBatchResultRejectedInner> rejected;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSketchBatchResult &&
      other.accepted == accepted &&
      other.rejected == rejected;

    @override
    int get hashCode =>
        accepted.hashCode +
        rejected.hashCode;

  factory ChronicleSketchBatchResult.fromJson(Map<String, dynamic> json) => _$ChronicleSketchBatchResultFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSketchBatchResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

