//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'external_binding_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExternalBindingEvent {
  /// Returns a new [ExternalBindingEvent] instance.
  ExternalBindingEvent({

    required  this.bindingId,

    required  this.sourceId,

    required  this.reason,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'bindingId',
    required: true,
    includeIfNull: false,
  )


  final String bindingId;



  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExternalBindingEvent &&
      other.bindingId == bindingId &&
      other.sourceId == sourceId &&
      other.reason == reason &&
      other.at == at;

    @override
    int get hashCode =>
        bindingId.hashCode +
        sourceId.hashCode +
        reason.hashCode +
        at.hashCode;

  factory ExternalBindingEvent.fromJson(Map<String, dynamic> json) => _$ExternalBindingEventFromJson(json);

  Map<String, dynamic> toJson() => _$ExternalBindingEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

