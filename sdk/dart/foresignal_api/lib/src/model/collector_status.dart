//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'collector_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CollectorStatus {
  /// Returns a new [CollectorStatus] instance.
  CollectorStatus({

    required  this.cap,

    required  this.inUse,

    required  this.waiting,
  });

      /// The governor's current cap: the maxConcurrentPulls in force.
          // minimum: 1
          // maximum: 16
  @JsonKey(
    
    name: r'cap',
    required: true,
    includeIfNull: false,
  )


  final int cap;



      /// How many polling fetches hold a permit and are pulling from upstream now. May briefly exceed cap just after the cap is lowered.
          // minimum: 0
  @JsonKey(
    
    name: r'inUse',
    required: true,
    includeIfNull: false,
  )


  final int inUse;



      /// How many polling fetches are queued for a permit.
          // minimum: 0
  @JsonKey(
    
    name: r'waiting',
    required: true,
    includeIfNull: false,
  )


  final int waiting;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CollectorStatus &&
      other.cap == cap &&
      other.inUse == inUse &&
      other.waiting == waiting;

    @override
    int get hashCode =>
        cap.hashCode +
        inUse.hashCode +
        waiting.hashCode;

  factory CollectorStatus.fromJson(Map<String, dynamic> json) => _$CollectorStatusFromJson(json);

  Map<String, dynamic> toJson() => _$CollectorStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

