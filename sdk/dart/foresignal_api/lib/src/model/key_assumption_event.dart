//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'key_assumption_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class KeyAssumptionEvent {
  /// Returns a new [KeyAssumptionEvent] instance.
  KeyAssumptionEvent({

    required  this.assumptionId,

    required  this.setId,

     this.text,

     this.linchpin,

    required  this.status,

     this.previousStatus,

     this.private,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'assumptionId',
    required: true,
    includeIfNull: false,
  )


  final String assumptionId;



      /// ULID identifier
  @JsonKey(
    
    name: r'setId',
    required: true,
    includeIfNull: false,
  )


  final String setId;



  @JsonKey(
    
    name: r'text',
    required: false,
    includeIfNull: false,
  )


  final String? text;



  @JsonKey(
    
    name: r'linchpin',
    required: false,
    includeIfNull: false,
  )


  final bool? linchpin;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final KeyAssumptionEventStatusEnum status;



  @JsonKey(
    
    name: r'previousStatus',
    required: false,
    includeIfNull: false,
  )


  final KeyAssumptionEventPreviousStatusEnum? previousStatus;



  @JsonKey(
    
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is KeyAssumptionEvent &&
      other.assumptionId == assumptionId &&
      other.setId == setId &&
      other.text == text &&
      other.linchpin == linchpin &&
      other.status == status &&
      other.previousStatus == previousStatus &&
      other.private == private &&
      other.at == at;

    @override
    int get hashCode =>
        assumptionId.hashCode +
        setId.hashCode +
        text.hashCode +
        linchpin.hashCode +
        status.hashCode +
        previousStatus.hashCode +
        private.hashCode +
        at.hashCode;

  factory KeyAssumptionEvent.fromJson(Map<String, dynamic> json) => _$KeyAssumptionEventFromJson(json);

  Map<String, dynamic> toJson() => _$KeyAssumptionEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum KeyAssumptionEventStatusEnum {
@JsonValue(r'solid')
solid(r'solid'),
@JsonValue(r'caveated')
caveated(r'caveated'),
@JsonValue(r'unsupported')
unsupported(r'unsupported'),
@JsonValue(r'broken')
broken(r'broken');

const KeyAssumptionEventStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum KeyAssumptionEventPreviousStatusEnum {
@JsonValue(r'solid')
solid(r'solid'),
@JsonValue(r'caveated')
caveated(r'caveated'),
@JsonValue(r'unsupported')
unsupported(r'unsupported'),
@JsonValue(r'broken')
broken(r'broken');

const KeyAssumptionEventPreviousStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


