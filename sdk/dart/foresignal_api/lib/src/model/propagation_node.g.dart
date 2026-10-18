// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'propagation_node.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PropagationNodeCWProxy {
  PropagationNode id(String id);

  PropagationNode type(PropagationNodeTypeEnum type);

  PropagationNode ref(String? ref);

  PropagationNode platform(String? platform);

  PropagationNode observedAt(DateTime observedAt);

  PropagationNode evidenceGroupId(String? evidenceGroupId);

  PropagationNode memberCount(int? memberCount);

  PropagationNode label(String? label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PropagationNode(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PropagationNode(...).copyWith(id: 12, name: "My name")
  /// ```
  PropagationNode call({
    String id,
    PropagationNodeTypeEnum type,
    String? ref,
    String? platform,
    DateTime observedAt,
    String? evidenceGroupId,
    int? memberCount,
    String? label,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPropagationNode.copyWith(...)` or call `instanceOfPropagationNode.copyWith.fieldName(value)` for a single field.
class _$PropagationNodeCWProxyImpl implements _$PropagationNodeCWProxy {
  const _$PropagationNodeCWProxyImpl(this._value);

  final PropagationNode _value;

  @override
  PropagationNode id(String id) => call(id: id);

  @override
  PropagationNode type(PropagationNodeTypeEnum type) => call(type: type);

  @override
  PropagationNode ref(String? ref) => call(ref: ref);

  @override
  PropagationNode platform(String? platform) => call(platform: platform);

  @override
  PropagationNode observedAt(DateTime observedAt) =>
      call(observedAt: observedAt);

  @override
  PropagationNode evidenceGroupId(String? evidenceGroupId) =>
      call(evidenceGroupId: evidenceGroupId);

  @override
  PropagationNode memberCount(int? memberCount) =>
      call(memberCount: memberCount);

  @override
  PropagationNode label(String? label) => call(label: label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PropagationNode(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PropagationNode(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PropagationNode call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? platform = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? evidenceGroupId = const $CopyWithPlaceholder(),
    Object? memberCount = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
  }) {
    return PropagationNode(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as PropagationNodeTypeEnum,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      platform: platform == const $CopyWithPlaceholder()
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String?,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
      evidenceGroupId: evidenceGroupId == const $CopyWithPlaceholder()
          ? _value.evidenceGroupId
          // ignore: cast_nullable_to_non_nullable
          : evidenceGroupId as String?,
      memberCount: memberCount == const $CopyWithPlaceholder()
          ? _value.memberCount
          // ignore: cast_nullable_to_non_nullable
          : memberCount as int?,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
    );
  }
}

extension $PropagationNodeCopyWith on PropagationNode {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPropagationNode.copyWith(...)` or `instanceOfPropagationNode.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PropagationNodeCWProxy get copyWith => _$PropagationNodeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PropagationNode _$PropagationNodeFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PropagationNode', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'type', 'observedAt']);
  final val = PropagationNode(
    id: $checkedConvert('id', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$PropagationNodeTypeEnumEnumMap, v),
    ),
    ref: $checkedConvert('ref', (v) => v as String?),
    platform: $checkedConvert('platform', (v) => v as String?),
    observedAt: $checkedConvert(
      'observedAt',
      (v) => DateTime.parse(v as String),
    ),
    evidenceGroupId: $checkedConvert('evidenceGroupId', (v) => v as String?),
    memberCount: $checkedConvert('memberCount', (v) => (v as num?)?.toInt()),
    label: $checkedConvert('label', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PropagationNodeToJson(PropagationNode instance) =>
    <String, dynamic>{
      'id': instance.id,
      'type': _$PropagationNodeTypeEnumEnumMap[instance.type]!,
      'ref': ?instance.ref,
      'platform': ?instance.platform,
      'observedAt': instance.observedAt.toIso8601String(),
      'evidenceGroupId': ?instance.evidenceGroupId,
      'memberCount': ?instance.memberCount,
      'label': ?instance.label,
    };

const _$PropagationNodeTypeEnumEnumMap = {
  PropagationNodeTypeEnum.post: 'post',
  PropagationNodeTypeEnum.evidenceGroup: 'evidence-group',
  PropagationNodeTypeEnum.newsArticle: 'news-article',
  PropagationNodeTypeEnum.officialRecord: 'official-record',
};
