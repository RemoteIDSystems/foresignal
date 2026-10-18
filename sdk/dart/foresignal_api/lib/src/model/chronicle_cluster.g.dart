// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_cluster.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleClusterCWProxy {
  ChronicleCluster id(String id);

  ChronicleCluster members(List<ChronicleClusterMembersInner> members);

  ChronicleCluster visibleMembers(int visibleMembers);

  ChronicleCluster opaqueMembers(int opaqueMembers);

  ChronicleCluster updatedAt(DateTime updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleCluster(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleCluster(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleCluster call({
    String id,
    List<ChronicleClusterMembersInner> members,
    int visibleMembers,
    int opaqueMembers,
    DateTime updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleCluster.copyWith(...)` or call `instanceOfChronicleCluster.copyWith.fieldName(value)` for a single field.
class _$ChronicleClusterCWProxyImpl implements _$ChronicleClusterCWProxy {
  const _$ChronicleClusterCWProxyImpl(this._value);

  final ChronicleCluster _value;

  @override
  ChronicleCluster id(String id) => call(id: id);

  @override
  ChronicleCluster members(List<ChronicleClusterMembersInner> members) =>
      call(members: members);

  @override
  ChronicleCluster visibleMembers(int visibleMembers) =>
      call(visibleMembers: visibleMembers);

  @override
  ChronicleCluster opaqueMembers(int opaqueMembers) =>
      call(opaqueMembers: opaqueMembers);

  @override
  ChronicleCluster updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleCluster(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleCluster(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleCluster call({
    Object? id = const $CopyWithPlaceholder(),
    Object? members = const $CopyWithPlaceholder(),
    Object? visibleMembers = const $CopyWithPlaceholder(),
    Object? opaqueMembers = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return ChronicleCluster(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      members: members == const $CopyWithPlaceholder() || members == null
          ? _value.members
          // ignore: cast_nullable_to_non_nullable
          : members as List<ChronicleClusterMembersInner>,
      visibleMembers:
          visibleMembers == const $CopyWithPlaceholder() ||
              visibleMembers == null
          ? _value.visibleMembers
          // ignore: cast_nullable_to_non_nullable
          : visibleMembers as int,
      opaqueMembers:
          opaqueMembers == const $CopyWithPlaceholder() || opaqueMembers == null
          ? _value.opaqueMembers
          // ignore: cast_nullable_to_non_nullable
          : opaqueMembers as int,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
    );
  }
}

extension $ChronicleClusterCopyWith on ChronicleCluster {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleCluster.copyWith(...)` or `instanceOfChronicleCluster.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleClusterCWProxy get copyWith => _$ChronicleClusterCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleCluster _$ChronicleClusterFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleCluster', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'members',
      'visibleMembers',
      'opaqueMembers',
      'updatedAt',
    ],
  );
  final val = ChronicleCluster(
    id: $checkedConvert('id', (v) => v as String),
    members: $checkedConvert(
      'members',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ChronicleClusterMembersInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    visibleMembers: $checkedConvert(
      'visibleMembers',
      (v) => (v as num).toInt(),
    ),
    opaqueMembers: $checkedConvert('opaqueMembers', (v) => (v as num).toInt()),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$ChronicleClusterToJson(ChronicleCluster instance) =>
    <String, dynamic>{
      'id': instance.id,
      'members': instance.members.map((e) => e.toJson()).toList(),
      'visibleMembers': instance.visibleMembers,
      'opaqueMembers': instance.opaqueMembers,
      'updatedAt': instance.updatedAt.toIso8601String(),
    };
