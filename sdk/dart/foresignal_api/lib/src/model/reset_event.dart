//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'reset_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ResetEvent {
  /// Returns a new [ResetEvent] instance.
  ResetEvent({

    required  this.reason,

     this.resumeAfter,

     this.watermarkSeq,
  });

  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final ResetEventReasonEnum reason;



      /// Last delivered seq to resume from (null for resync_required)
  @JsonKey(
    
    name: r'resumeAfter',
    required: false,
    includeIfNull: false,
  )


  final int? resumeAfter;



  @JsonKey(
    
    name: r'watermarkSeq',
    required: false,
    includeIfNull: false,
  )


  final int? watermarkSeq;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ResetEvent &&
      other.reason == reason &&
      other.resumeAfter == resumeAfter &&
      other.watermarkSeq == watermarkSeq;

    @override
    int get hashCode =>
        reason.hashCode +
        (resumeAfter == null ? 0 : resumeAfter.hashCode) +
        (watermarkSeq == null ? 0 : watermarkSeq.hashCode);

  factory ResetEvent.fromJson(Map<String, dynamic> json) => _$ResetEventFromJson(json);

  Map<String, dynamic> toJson() => _$ResetEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ResetEventReasonEnum {
@JsonValue(r'buffer_overflow')
bufferOverflow(r'buffer_overflow'),
@JsonValue(r'replay_cap')
replayCap(r'replay_cap'),
@JsonValue(r'resync_required')
resyncRequired(r'resync_required');

const ResetEventReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


