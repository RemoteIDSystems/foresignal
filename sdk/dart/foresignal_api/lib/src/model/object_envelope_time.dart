//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'object_envelope_time.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ObjectEnvelopeTime {
  /// Returns a new [ObjectEnvelopeTime] instance.
  ObjectEnvelopeTime({

     this.start,

     this.end,

     this.observedAt,
  });

  @JsonKey(
    
    name: r'start',
    required: false,
    includeIfNull: false,
  )


  final DateTime? start;



  @JsonKey(
    
    name: r'end',
    required: false,
    includeIfNull: false,
  )


  final DateTime? end;



  @JsonKey(
    
    name: r'observedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? observedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ObjectEnvelopeTime &&
      other.start == start &&
      other.end == end &&
      other.observedAt == observedAt;

    @override
    int get hashCode =>
        (start == null ? 0 : start.hashCode) +
        (end == null ? 0 : end.hashCode) +
        (observedAt == null ? 0 : observedAt.hashCode);

  factory ObjectEnvelopeTime.fromJson(Map<String, dynamic> json) => _$ObjectEnvelopeTimeFromJson(json);

  Map<String, dynamic> toJson() => _$ObjectEnvelopeTimeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

