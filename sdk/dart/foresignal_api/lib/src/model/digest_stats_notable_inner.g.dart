// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'digest_stats_notable_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DigestStatsNotableInnerCWProxy {
  DigestStatsNotableInner kind(String kind);

  DigestStatsNotableInner items(List<DigestNotableItem> items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestStatsNotableInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestStatsNotableInner(...).copyWith(id: 12, name: "My name")
  /// ```
  DigestStatsNotableInner call({String kind, List<DigestNotableItem> items});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDigestStatsNotableInner.copyWith(...)` or call `instanceOfDigestStatsNotableInner.copyWith.fieldName(value)` for a single field.
class _$DigestStatsNotableInnerCWProxyImpl
    implements _$DigestStatsNotableInnerCWProxy {
  const _$DigestStatsNotableInnerCWProxyImpl(this._value);

  final DigestStatsNotableInner _value;

  @override
  DigestStatsNotableInner kind(String kind) => call(kind: kind);

  @override
  DigestStatsNotableInner items(List<DigestNotableItem> items) =>
      call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestStatsNotableInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestStatsNotableInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DigestStatsNotableInner call({
    Object? kind = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return DigestStatsNotableInner(
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<DigestNotableItem>,
    );
  }
}

extension $DigestStatsNotableInnerCopyWith on DigestStatsNotableInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDigestStatsNotableInner.copyWith(...)` or `instanceOfDigestStatsNotableInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DigestStatsNotableInnerCWProxy get copyWith =>
      _$DigestStatsNotableInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DigestStatsNotableInner _$DigestStatsNotableInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DigestStatsNotableInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['kind', 'items']);
  final val = DigestStatsNotableInner(
    kind: $checkedConvert('kind', (v) => v as String),
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => DigestNotableItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$DigestStatsNotableInnerToJson(
  DigestStatsNotableInner instance,
) => <String, dynamic>{
  'kind': instance.kind,
  'items': instance.items.map((e) => e.toJson()).toList(),
};
