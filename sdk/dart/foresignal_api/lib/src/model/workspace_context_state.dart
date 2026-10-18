//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/workspace_context.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'workspace_context_state.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WorkspaceContextState {
  /// Returns a new [WorkspaceContextState] instance.
  WorkspaceContextState({

    required  this.workspaceId,

    required  this.contextVersion,

    required  this.context,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'workspaceId',
    required: true,
    includeIfNull: false,
  )


  final String workspaceId;



  @JsonKey(
    
    name: r'contextVersion',
    required: true,
    includeIfNull: false,
  )


  final int contextVersion;



  @JsonKey(
    
    name: r'context',
    required: true,
    includeIfNull: false,
  )


  final WorkspaceContext context;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WorkspaceContextState &&
      other.workspaceId == workspaceId &&
      other.contextVersion == contextVersion &&
      other.context == context;

    @override
    int get hashCode =>
        workspaceId.hashCode +
        contextVersion.hashCode +
        context.hashCode;

  factory WorkspaceContextState.fromJson(Map<String, dynamic> json) => _$WorkspaceContextStateFromJson(json);

  Map<String, dynamic> toJson() => _$WorkspaceContextStateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

