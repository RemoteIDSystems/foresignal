//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'filter_applied_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FilterAppliedEvent {
  /// Returns a new [FilterAppliedEvent] instance.
  FilterAppliedEvent({

    required  this.atSeq,

    required  this.filterVersion,
  });

      /// Last change seq delivered under the old filter
  @JsonKey(
    
    name: r'atSeq',
    required: true,
    includeIfNull: false,
  )


  final int atSeq;



          // minimum: 1
  @JsonKey(
    
    name: r'filterVersion',
    required: true,
    includeIfNull: false,
  )


  final int filterVersion;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FilterAppliedEvent &&
      other.atSeq == atSeq &&
      other.filterVersion == filterVersion;

    @override
    int get hashCode =>
        atSeq.hashCode +
        filterVersion.hashCode;

  factory FilterAppliedEvent.fromJson(Map<String, dynamic> json) => _$FilterAppliedEventFromJson(json);

  Map<String, dynamic> toJson() => _$FilterAppliedEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

