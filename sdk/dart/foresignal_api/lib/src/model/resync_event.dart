//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'resync_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ResyncEvent {
  /// Returns a new [ResyncEvent] instance.
  ResyncEvent({

    required  this.reason,

    required  this.watermarkSeq,
  });

  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final ResyncEventReasonEnum reason;



  @JsonKey(
    
    name: r'watermarkSeq',
    required: true,
    includeIfNull: false,
  )


  final int watermarkSeq;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ResyncEvent &&
      other.reason == reason &&
      other.watermarkSeq == watermarkSeq;

    @override
    int get hashCode =>
        reason.hashCode +
        watermarkSeq.hashCode;

  factory ResyncEvent.fromJson(Map<String, dynamic> json) => _$ResyncEventFromJson(json);

  Map<String, dynamic> toJson() => _$ResyncEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ResyncEventReasonEnum {
@JsonValue(r'gap')
gap(r'gap'),
@JsonValue(r'live_cap')
liveCap(r'live_cap'),
@JsonValue(r'live_dropped')
liveDropped(r'live_dropped');

const ResyncEventReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


