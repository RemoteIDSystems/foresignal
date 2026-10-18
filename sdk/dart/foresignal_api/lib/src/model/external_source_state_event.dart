//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'external_source_state_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExternalSourceStateEvent {
  /// Returns a new [ExternalSourceStateEvent] instance.
  ExternalSourceStateEvent({

    required  this.sourceId,

     this.state,

    required  this.change,

    required  this.at,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'state',
    required: false,
    includeIfNull: false,
  )


  final String? state;



  @JsonKey(
    
    name: r'change',
    required: true,
    includeIfNull: false,
  )


  final ExternalSourceStateEventChangeEnum change;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExternalSourceStateEvent &&
      other.sourceId == sourceId &&
      other.state == state &&
      other.change == change &&
      other.at == at;

    @override
    int get hashCode =>
        sourceId.hashCode +
        state.hashCode +
        change.hashCode +
        at.hashCode;

  factory ExternalSourceStateEvent.fromJson(Map<String, dynamic> json) => _$ExternalSourceStateEventFromJson(json);

  Map<String, dynamic> toJson() => _$ExternalSourceStateEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExternalSourceStateEventChangeEnum {
@JsonValue(r'registered')
registered(r'registered'),
@JsonValue(r'approved')
approved(r'approved'),
@JsonValue(r'suspended')
suspended(r'suspended'),
@JsonValue(r'manifest-changed')
manifestChanged(r'manifest-changed'),
@JsonValue(r'deleted')
deleted(r'deleted');

const ExternalSourceStateEventChangeEnum(this.value);

final String value;

@override
String toString() => value;
}


