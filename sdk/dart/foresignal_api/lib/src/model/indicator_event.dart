//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'indicator_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IndicatorEvent {
  /// Returns a new [IndicatorEvent] instance.
  IndicatorEvent({

    required  this.indicatorId,

    required  this.setId,

     this.description,

     this.signalFamily,

    required  this.state,

     this.previousState,

     this.evidence,

     this.revisionId,

     this.private,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'indicatorId',
    required: true,
    includeIfNull: false,
  )


  final String indicatorId;



      /// ULID identifier
  @JsonKey(
    
    name: r'setId',
    required: true,
    includeIfNull: false,
  )


  final String setId;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'signalFamily',
    required: false,
    includeIfNull: false,
  )


  final String? signalFamily;



  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final IndicatorEventStateEnum state;



  @JsonKey(
    
    name: r'previousState',
    required: false,
    includeIfNull: false,
  )


  final IndicatorEventPreviousStateEnum? previousState;



  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? evidence;



      /// ULID identifier
  @JsonKey(
    
    name: r'revisionId',
    required: false,
    includeIfNull: false,
  )


  final String? revisionId;



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
    bool operator ==(Object other) => identical(this, other) || other is IndicatorEvent &&
      other.indicatorId == indicatorId &&
      other.setId == setId &&
      other.description == description &&
      other.signalFamily == signalFamily &&
      other.state == state &&
      other.previousState == previousState &&
      other.evidence == evidence &&
      other.revisionId == revisionId &&
      other.private == private &&
      other.at == at;

    @override
    int get hashCode =>
        indicatorId.hashCode +
        setId.hashCode +
        description.hashCode +
        signalFamily.hashCode +
        state.hashCode +
        previousState.hashCode +
        (evidence == null ? 0 : evidence.hashCode) +
        (revisionId == null ? 0 : revisionId.hashCode) +
        private.hashCode +
        at.hashCode;

  factory IndicatorEvent.fromJson(Map<String, dynamic> json) => _$IndicatorEventFromJson(json);

  Map<String, dynamic> toJson() => _$IndicatorEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IndicatorEventStateEnum {
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'emerging')
emerging(r'emerging'),
@JsonValue(r'observed')
observed(r'observed'),
@JsonValue(r'absent')
absent(r'absent'),
@JsonValue(r'unknown-coverage')
unknownCoverage(r'unknown-coverage'),
@JsonValue(r'retired')
retired(r'retired'),
@JsonValue(r'reported')
reported(r'reported');

const IndicatorEventStateEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IndicatorEventPreviousStateEnum {
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'emerging')
emerging(r'emerging'),
@JsonValue(r'observed')
observed(r'observed'),
@JsonValue(r'absent')
absent(r'absent'),
@JsonValue(r'unknown-coverage')
unknownCoverage(r'unknown-coverage'),
@JsonValue(r'retired')
retired(r'retired'),
@JsonValue(r'reported')
reported(r'reported');

const IndicatorEventPreviousStateEnum(this.value);

final String value;

@override
String toString() => value;
}


