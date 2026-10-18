// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'propagation_edge.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PropagationEdgeCWProxy {
  PropagationEdge from(String from);

  PropagationEdge to(String to);

  PropagationEdge type(PropagationEdgeTypeEnum type);

  PropagationEdge deltaSeconds(num deltaSeconds);

  PropagationEdge count(int? count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PropagationEdge(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PropagationEdge(...).copyWith(id: 12, name: "My name")
  /// ```
  PropagationEdge call({
    String from,
    String to,
    PropagationEdgeTypeEnum type,
    num deltaSeconds,
    int? count,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPropagationEdge.copyWith(...)` or call `instanceOfPropagationEdge.copyWith.fieldName(value)` for a single field.
class _$PropagationEdgeCWProxyImpl implements _$PropagationEdgeCWProxy {
  const _$PropagationEdgeCWProxyImpl(this._value);

  final PropagationEdge _value;

  @override
  PropagationEdge from(String from) => call(from: from);

  @override
  PropagationEdge to(String to) => call(to: to);

  @override
  PropagationEdge type(PropagationEdgeTypeEnum type) => call(type: type);

  @override
  PropagationEdge deltaSeconds(num deltaSeconds) =>
      call(deltaSeconds: deltaSeconds);

  @override
  PropagationEdge count(int? count) => call(count: count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PropagationEdge(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PropagationEdge(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PropagationEdge call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? deltaSeconds = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
  }) {
    return PropagationEdge(
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as String,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as PropagationEdgeTypeEnum,
      deltaSeconds:
          deltaSeconds == const $CopyWithPlaceholder() || deltaSeconds == null
          ? _value.deltaSeconds
          // ignore: cast_nullable_to_non_nullable
          : deltaSeconds as num,
      count: count == const $CopyWithPlaceholder()
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int?,
    );
  }
}

extension $PropagationEdgeCopyWith on PropagationEdge {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPropagationEdge.copyWith(...)` or `instanceOfPropagationEdge.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PropagationEdgeCWProxy get copyWith => _$PropagationEdgeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PropagationEdge _$PropagationEdgeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PropagationEdge', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['from', 'to', 'type', 'deltaSeconds'],
      );
      final val = PropagationEdge(
        from: $checkedConvert('from', (v) => v as String),
        to: $checkedConvert('to', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$PropagationEdgeTypeEnumEnumMap, v),
        ),
        deltaSeconds: $checkedConvert('deltaSeconds', (v) => v as num),
        count: $checkedConvert('count', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$PropagationEdgeToJson(PropagationEdge instance) =>
    <String, dynamic>{
      'from': instance.from,
      'to': instance.to,
      'type': _$PropagationEdgeTypeEnumEnumMap[instance.type]!,
      'deltaSeconds': instance.deltaSeconds,
      'count': ?instance.count,
    };

const _$PropagationEdgeTypeEnumEnumMap = {
  PropagationEdgeTypeEnum.repost: 'repost',
  PropagationEdgeTypeEnum.quote: 'quote',
  PropagationEdgeTypeEnum.reply: 'reply',
  PropagationEdgeTypeEnum.link: 'link',
  PropagationEdgeTypeEnum.textMatch: 'text-match',
  PropagationEdgeTypeEnum.mediaMatch: 'media-match',
  PropagationEdgeTypeEnum.crossPlatform: 'cross-platform',
};
