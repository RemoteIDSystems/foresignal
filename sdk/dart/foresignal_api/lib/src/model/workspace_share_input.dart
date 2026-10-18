//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'workspace_share_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WorkspaceShareInput {
  /// Returns a new [WorkspaceShareInput] instance.
  WorkspaceShareInput({

     this.expiresAt,

     this.maxUses,
  });

  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;



          // minimum: 1
  @JsonKey(
    
    name: r'maxUses',
    required: false,
    includeIfNull: false,
  )


  final int? maxUses;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WorkspaceShareInput &&
      other.expiresAt == expiresAt &&
      other.maxUses == maxUses;

    @override
    int get hashCode =>
        (expiresAt == null ? 0 : expiresAt.hashCode) +
        (maxUses == null ? 0 : maxUses.hashCode);

  factory WorkspaceShareInput.fromJson(Map<String, dynamic> json) => _$WorkspaceShareInputFromJson(json);

  Map<String, dynamic> toJson() => _$WorkspaceShareInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

