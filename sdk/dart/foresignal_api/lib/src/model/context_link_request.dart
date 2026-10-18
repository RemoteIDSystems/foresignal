//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/workspace_context.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'context_link_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ContextLinkRequest {
  /// Returns a new [ContextLinkRequest] instance.
  ContextLinkRequest({

    required  this.context,

     this.workspaceId,
  });

  @JsonKey(
    
    name: r'context',
    required: true,
    includeIfNull: false,
  )


  final WorkspaceContext context;



      /// ULID identifier
  @JsonKey(
    
    name: r'workspaceId',
    required: false,
    includeIfNull: false,
  )


  final String? workspaceId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ContextLinkRequest &&
      other.context == context &&
      other.workspaceId == workspaceId;

    @override
    int get hashCode =>
        context.hashCode +
        (workspaceId == null ? 0 : workspaceId.hashCode);

  factory ContextLinkRequest.fromJson(Map<String, dynamic> json) => _$ContextLinkRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ContextLinkRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

