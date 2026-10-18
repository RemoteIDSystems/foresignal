//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_cluster_members_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_cluster.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleCluster {
  /// Returns a new [ChronicleCluster] instance.
  ChronicleCluster({

    required  this.id,

    required  this.members,

    required  this.visibleMembers,

    required  this.opaqueMembers,

    required  this.updatedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'members',
    required: true,
    includeIfNull: false,
  )


  final List<ChronicleClusterMembersInner> members;



  @JsonKey(
    
    name: r'visibleMembers',
    required: true,
    includeIfNull: false,
  )


  final int visibleMembers;



  @JsonKey(
    
    name: r'opaqueMembers',
    required: true,
    includeIfNull: false,
  )


  final int opaqueMembers;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleCluster &&
      other.id == id &&
      other.members == members &&
      other.visibleMembers == visibleMembers &&
      other.opaqueMembers == opaqueMembers &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        members.hashCode +
        visibleMembers.hashCode +
        opaqueMembers.hashCode +
        updatedAt.hashCode;

  factory ChronicleCluster.fromJson(Map<String, dynamic> json) => _$ChronicleClusterFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleClusterToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

