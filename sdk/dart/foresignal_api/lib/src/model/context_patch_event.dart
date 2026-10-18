//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'context_patch_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ContextPatchEvent {
  /// Returns a new [ContextPatchEvent] instance.
  ContextPatchEvent({

    required  this.workspaceId,

    required  this.version,

    required  this.patch_,

     this.originClientId,

     this.principal,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'workspaceId',
    required: true,
    includeIfNull: false,
  )


  final String workspaceId;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



      /// JSON merge patch applied
  @JsonKey(
    
    name: r'patch',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> patch_;



  @JsonKey(
    
    name: r'originClientId',
    required: false,
    includeIfNull: false,
  )


  final String? originClientId;



  @JsonKey(
    
    name: r'principal',
    required: false,
    includeIfNull: false,
  )


  final String? principal;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ContextPatchEvent &&
      other.workspaceId == workspaceId &&
      other.version == version &&
      other.patch_ == patch_ &&
      other.originClientId == originClientId &&
      other.principal == principal &&
      other.at == at;

    @override
    int get hashCode =>
        workspaceId.hashCode +
        version.hashCode +
        patch_.hashCode +
        (originClientId == null ? 0 : originClientId.hashCode) +
        (principal == null ? 0 : principal.hashCode) +
        at.hashCode;

  factory ContextPatchEvent.fromJson(Map<String, dynamic> json) => _$ContextPatchEventFromJson(json);

  Map<String, dynamic> toJson() => _$ContextPatchEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

