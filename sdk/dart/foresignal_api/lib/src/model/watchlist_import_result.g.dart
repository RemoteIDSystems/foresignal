// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'watchlist_import_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WatchlistImportResultCWProxy {
  WatchlistImportResult dryRun(bool dryRun);

  WatchlistImportResult mode(WatchlistImportResultModeEnum? mode);

  WatchlistImportResult added(int added);

  WatchlistImportResult updated(int updated);

  WatchlistImportResult removed(int removed);

  WatchlistImportResult unchanged(int? unchanged);

  WatchlistImportResult invalid(
    List<WatchlistImportResultInvalidInner> invalid,
  );

  WatchlistImportResult itemCount(int itemCount);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchlistImportResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchlistImportResult(...).copyWith(id: 12, name: "My name")
  /// ```
  WatchlistImportResult call({
    bool dryRun,
    WatchlistImportResultModeEnum? mode,
    int added,
    int updated,
    int removed,
    int? unchanged,
    List<WatchlistImportResultInvalidInner> invalid,
    int itemCount,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWatchlistImportResult.copyWith(...)` or call `instanceOfWatchlistImportResult.copyWith.fieldName(value)` for a single field.
class _$WatchlistImportResultCWProxyImpl
    implements _$WatchlistImportResultCWProxy {
  const _$WatchlistImportResultCWProxyImpl(this._value);

  final WatchlistImportResult _value;

  @override
  WatchlistImportResult dryRun(bool dryRun) => call(dryRun: dryRun);

  @override
  WatchlistImportResult mode(WatchlistImportResultModeEnum? mode) =>
      call(mode: mode);

  @override
  WatchlistImportResult added(int added) => call(added: added);

  @override
  WatchlistImportResult updated(int updated) => call(updated: updated);

  @override
  WatchlistImportResult removed(int removed) => call(removed: removed);

  @override
  WatchlistImportResult unchanged(int? unchanged) => call(unchanged: unchanged);

  @override
  WatchlistImportResult invalid(
    List<WatchlistImportResultInvalidInner> invalid,
  ) => call(invalid: invalid);

  @override
  WatchlistImportResult itemCount(int itemCount) => call(itemCount: itemCount);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchlistImportResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchlistImportResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WatchlistImportResult call({
    Object? dryRun = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? added = const $CopyWithPlaceholder(),
    Object? updated = const $CopyWithPlaceholder(),
    Object? removed = const $CopyWithPlaceholder(),
    Object? unchanged = const $CopyWithPlaceholder(),
    Object? invalid = const $CopyWithPlaceholder(),
    Object? itemCount = const $CopyWithPlaceholder(),
  }) {
    return WatchlistImportResult(
      dryRun: dryRun == const $CopyWithPlaceholder() || dryRun == null
          ? _value.dryRun
          // ignore: cast_nullable_to_non_nullable
          : dryRun as bool,
      mode: mode == const $CopyWithPlaceholder()
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as WatchlistImportResultModeEnum?,
      added: added == const $CopyWithPlaceholder() || added == null
          ? _value.added
          // ignore: cast_nullable_to_non_nullable
          : added as int,
      updated: updated == const $CopyWithPlaceholder() || updated == null
          ? _value.updated
          // ignore: cast_nullable_to_non_nullable
          : updated as int,
      removed: removed == const $CopyWithPlaceholder() || removed == null
          ? _value.removed
          // ignore: cast_nullable_to_non_nullable
          : removed as int,
      unchanged: unchanged == const $CopyWithPlaceholder()
          ? _value.unchanged
          // ignore: cast_nullable_to_non_nullable
          : unchanged as int?,
      invalid: invalid == const $CopyWithPlaceholder() || invalid == null
          ? _value.invalid
          // ignore: cast_nullable_to_non_nullable
          : invalid as List<WatchlistImportResultInvalidInner>,
      itemCount: itemCount == const $CopyWithPlaceholder() || itemCount == null
          ? _value.itemCount
          // ignore: cast_nullable_to_non_nullable
          : itemCount as int,
    );
  }
}

extension $WatchlistImportResultCopyWith on WatchlistImportResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWatchlistImportResult.copyWith(...)` or `instanceOfWatchlistImportResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WatchlistImportResultCWProxy get copyWith =>
      _$WatchlistImportResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WatchlistImportResult _$WatchlistImportResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('WatchlistImportResult', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'dryRun',
      'added',
      'updated',
      'removed',
      'invalid',
      'itemCount',
    ],
  );
  final val = WatchlistImportResult(
    dryRun: $checkedConvert('dryRun', (v) => v as bool),
    mode: $checkedConvert(
      'mode',
      (v) => $enumDecodeNullable(_$WatchlistImportResultModeEnumEnumMap, v),
    ),
    added: $checkedConvert('added', (v) => (v as num).toInt()),
    updated: $checkedConvert('updated', (v) => (v as num).toInt()),
    removed: $checkedConvert('removed', (v) => (v as num).toInt()),
    unchanged: $checkedConvert('unchanged', (v) => (v as num?)?.toInt()),
    invalid: $checkedConvert(
      'invalid',
      (v) => (v as List<dynamic>)
          .map(
            (e) => WatchlistImportResultInvalidInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    itemCount: $checkedConvert('itemCount', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$WatchlistImportResultToJson(
  WatchlistImportResult instance,
) => <String, dynamic>{
  'dryRun': instance.dryRun,
  'mode': ?_$WatchlistImportResultModeEnumEnumMap[instance.mode],
  'added': instance.added,
  'updated': instance.updated,
  'removed': instance.removed,
  'unchanged': ?instance.unchanged,
  'invalid': instance.invalid.map((e) => e.toJson()).toList(),
  'itemCount': instance.itemCount,
};

const _$WatchlistImportResultModeEnumEnumMap = {
  WatchlistImportResultModeEnum.merge: 'merge',
  WatchlistImportResultModeEnum.replace: 'replace',
};
