//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/cpa_result.dart';
import 'package:foresignal_api/src/model/kinematic_state.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cpa_batch_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CpaBatchResult {
  /// Returns a new [CpaBatchResult] instance.
  CpaBatchResult({

    required  this.subject,

    required  this.asOf,

    required  this.evaluated,

     this.truncated,

    required  this.items,
  });

  @JsonKey(
    
    name: r'subject',
    required: true,
    includeIfNull: false,
  )


  final KinematicState subject;



  @JsonKey(
    
    name: r'asOf',
    required: true,
    includeIfNull: false,
  )


  final DateTime asOf;



  @JsonKey(
    
    name: r'evaluated',
    required: true,
    includeIfNull: false,
  )


  final int evaluated;



      /// More than 500 candidates matched the filter
  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<CpaResult> items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CpaBatchResult &&
      other.subject == subject &&
      other.asOf == asOf &&
      other.evaluated == evaluated &&
      other.truncated == truncated &&
      other.items == items;

    @override
    int get hashCode =>
        subject.hashCode +
        asOf.hashCode +
        evaluated.hashCode +
        truncated.hashCode +
        items.hashCode;

  factory CpaBatchResult.fromJson(Map<String, dynamic> json) => _$CpaBatchResultFromJson(json);

  Map<String, dynamic> toJson() => _$CpaBatchResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

