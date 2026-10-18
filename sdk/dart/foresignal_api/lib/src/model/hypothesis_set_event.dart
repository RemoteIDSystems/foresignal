//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/hypothesis_probability_state.dart';
import 'package:foresignal_api/src/model/hypothesis_set_event_band_changes_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_set_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisSetEvent {
  /// Returns a new [HypothesisSetEvent] instance.
  HypothesisSetEvent({

    required  this.setId,

    required  this.version,

     this.question,

    required  this.status,

     this.revisionId,

     this.cause,

     this.hypotheses,

     this.bandChanges,

     this.private,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'setId',
    required: true,
    includeIfNull: false,
  )


  final String setId;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'question',
    required: false,
    includeIfNull: false,
  )


  final String? question;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final HypothesisSetEventStatusEnum status;



      /// ULID identifier
  @JsonKey(
    
    name: r'revisionId',
    required: false,
    includeIfNull: false,
  )


  final String? revisionId;



  @JsonKey(
    
    name: r'cause',
    required: false,
    includeIfNull: false,
  )


  final Object? cause;



  @JsonKey(
    
    name: r'hypotheses',
    required: false,
    includeIfNull: false,
  )


  final List<HypothesisProbabilityState>? hypotheses;



  @JsonKey(
    
    name: r'bandChanges',
    required: false,
    includeIfNull: false,
  )


  final List<HypothesisSetEventBandChangesInner>? bandChanges;



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
    bool operator ==(Object other) => identical(this, other) || other is HypothesisSetEvent &&
      other.setId == setId &&
      other.version == version &&
      other.question == question &&
      other.status == status &&
      other.revisionId == revisionId &&
      other.cause == cause &&
      other.hypotheses == hypotheses &&
      other.bandChanges == bandChanges &&
      other.private == private &&
      other.at == at;

    @override
    int get hashCode =>
        setId.hashCode +
        version.hashCode +
        question.hashCode +
        status.hashCode +
        (revisionId == null ? 0 : revisionId.hashCode) +
        (cause == null ? 0 : cause.hashCode) +
        hypotheses.hashCode +
        bandChanges.hashCode +
        private.hashCode +
        at.hashCode;

  factory HypothesisSetEvent.fromJson(Map<String, dynamic> json) => _$HypothesisSetEventFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisSetEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum HypothesisSetEventStatusEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'resolved')
resolved(r'resolved'),
@JsonValue(r'archived')
archived(r'archived');

const HypothesisSetEventStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


