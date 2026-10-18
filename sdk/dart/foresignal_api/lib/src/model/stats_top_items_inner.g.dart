// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stats_top_items_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StatsTopItemsInnerCWProxy {
  StatsTopItemsInner key(String key);

  StatsTopItemsInner sourceId(String? sourceId);

  StatsTopItemsInner kind(String? kind);

  StatsTopItemsInner cell(String? cell);

  StatsTopItemsInner externalId(String? externalId);

  StatsTopItemsInner title(String? title);

  StatsTopItemsInner count(int count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsTopItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsTopItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  StatsTopItemsInner call({
    String key,
    String? sourceId,
    String? kind,
    String? cell,
    String? externalId,
    String? title,
    int count,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStatsTopItemsInner.copyWith(...)` or call `instanceOfStatsTopItemsInner.copyWith.fieldName(value)` for a single field.
class _$StatsTopItemsInnerCWProxyImpl implements _$StatsTopItemsInnerCWProxy {
  const _$StatsTopItemsInnerCWProxyImpl(this._value);

  final StatsTopItemsInner _value;

  @override
  StatsTopItemsInner key(String key) => call(key: key);

  @override
  StatsTopItemsInner sourceId(String? sourceId) => call(sourceId: sourceId);

  @override
  StatsTopItemsInner kind(String? kind) => call(kind: kind);

  @override
  StatsTopItemsInner cell(String? cell) => call(cell: cell);

  @override
  StatsTopItemsInner externalId(String? externalId) =>
      call(externalId: externalId);

  @override
  StatsTopItemsInner title(String? title) => call(title: title);

  @override
  StatsTopItemsInner count(int count) => call(count: count);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsTopItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsTopItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StatsTopItemsInner call({
    Object? key = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? cell = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
  }) {
    return StatsTopItemsInner(
      key: key == const $CopyWithPlaceholder() || key == null
          ? _value.key
          // ignore: cast_nullable_to_non_nullable
          : key as String,
      sourceId: sourceId == const $CopyWithPlaceholder()
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String?,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String?,
      cell: cell == const $CopyWithPlaceholder()
          ? _value.cell
          // ignore: cast_nullable_to_non_nullable
          : cell as String?,
      externalId: externalId == const $CopyWithPlaceholder()
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String?,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
    );
  }
}

extension $StatsTopItemsInnerCopyWith on StatsTopItemsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStatsTopItemsInner.copyWith(...)` or `instanceOfStatsTopItemsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StatsTopItemsInnerCWProxy get copyWith =>
      _$StatsTopItemsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StatsTopItemsInner _$StatsTopItemsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StatsTopItemsInner', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['key', 'count']);
      final val = StatsTopItemsInner(
        key: $checkedConvert('key', (v) => v as String),
        sourceId: $checkedConvert('sourceId', (v) => v as String?),
        kind: $checkedConvert('kind', (v) => v as String?),
        cell: $checkedConvert('cell', (v) => v as String?),
        externalId: $checkedConvert('externalId', (v) => v as String?),
        title: $checkedConvert('title', (v) => v as String?),
        count: $checkedConvert('count', (v) => (v as num).toInt()),
      );
      return val;
    });

Map<String, dynamic> _$StatsTopItemsInnerToJson(StatsTopItemsInner instance) =>
    <String, dynamic>{
      'key': instance.key,
      'sourceId': ?instance.sourceId,
      'kind': ?instance.kind,
      'cell': ?instance.cell,
      'externalId': ?instance.externalId,
      'title': ?instance.title,
      'count': instance.count,
    };
