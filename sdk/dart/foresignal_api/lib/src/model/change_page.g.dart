// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'change_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChangePageCWProxy {
  ChangePage items(List<Change> items);

  ChangePage lastSeq(int lastSeq);

  ChangePage hasMore(bool hasMore);

  ChangePage watermarkSeq(int? watermarkSeq);

  ChangePage purgeHorizon(int? purgeHorizon);

  ChangePage attributions(List<Attribution>? attributions);

  ChangePage licenceFiltered(int? licenceFiltered);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChangePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChangePage(...).copyWith(id: 12, name: "My name")
  /// ```
  ChangePage call({
    List<Change> items,
    int lastSeq,
    bool hasMore,
    int? watermarkSeq,
    int? purgeHorizon,
    List<Attribution>? attributions,
    int? licenceFiltered,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChangePage.copyWith(...)` or call `instanceOfChangePage.copyWith.fieldName(value)` for a single field.
class _$ChangePageCWProxyImpl implements _$ChangePageCWProxy {
  const _$ChangePageCWProxyImpl(this._value);

  final ChangePage _value;

  @override
  ChangePage items(List<Change> items) => call(items: items);

  @override
  ChangePage lastSeq(int lastSeq) => call(lastSeq: lastSeq);

  @override
  ChangePage hasMore(bool hasMore) => call(hasMore: hasMore);

  @override
  ChangePage watermarkSeq(int? watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  @override
  ChangePage purgeHorizon(int? purgeHorizon) =>
      call(purgeHorizon: purgeHorizon);

  @override
  ChangePage attributions(List<Attribution>? attributions) =>
      call(attributions: attributions);

  @override
  ChangePage licenceFiltered(int? licenceFiltered) =>
      call(licenceFiltered: licenceFiltered);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChangePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChangePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChangePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? lastSeq = const $CopyWithPlaceholder(),
    Object? hasMore = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
    Object? purgeHorizon = const $CopyWithPlaceholder(),
    Object? attributions = const $CopyWithPlaceholder(),
    Object? licenceFiltered = const $CopyWithPlaceholder(),
  }) {
    return ChangePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Change>,
      lastSeq: lastSeq == const $CopyWithPlaceholder() || lastSeq == null
          ? _value.lastSeq
          // ignore: cast_nullable_to_non_nullable
          : lastSeq as int,
      hasMore: hasMore == const $CopyWithPlaceholder() || hasMore == null
          ? _value.hasMore
          // ignore: cast_nullable_to_non_nullable
          : hasMore as bool,
      watermarkSeq: watermarkSeq == const $CopyWithPlaceholder()
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int?,
      purgeHorizon: purgeHorizon == const $CopyWithPlaceholder()
          ? _value.purgeHorizon
          // ignore: cast_nullable_to_non_nullable
          : purgeHorizon as int?,
      attributions: attributions == const $CopyWithPlaceholder()
          ? _value.attributions
          // ignore: cast_nullable_to_non_nullable
          : attributions as List<Attribution>?,
      licenceFiltered: licenceFiltered == const $CopyWithPlaceholder()
          ? _value.licenceFiltered
          // ignore: cast_nullable_to_non_nullable
          : licenceFiltered as int?,
    );
  }
}

extension $ChangePageCopyWith on ChangePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChangePage.copyWith(...)` or `instanceOfChangePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChangePageCWProxy get copyWith => _$ChangePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChangePage _$ChangePageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChangePage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items', 'lastSeq', 'hasMore']);
  final val = ChangePage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => Change.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    lastSeq: $checkedConvert('lastSeq', (v) => (v as num).toInt()),
    hasMore: $checkedConvert('hasMore', (v) => v as bool),
    watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num?)?.toInt()),
    purgeHorizon: $checkedConvert('purgeHorizon', (v) => (v as num?)?.toInt()),
    attributions: $checkedConvert(
      'attributions',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Attribution.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    licenceFiltered: $checkedConvert(
      'licenceFiltered',
      (v) => (v as num?)?.toInt(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChangePageToJson(ChangePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'lastSeq': instance.lastSeq,
      'hasMore': instance.hasMore,
      'watermarkSeq': ?instance.watermarkSeq,
      'purgeHorizon': ?instance.purgeHorizon,
      'attributions': ?instance.attributions?.map((e) => e.toJson()).toList(),
      'licenceFiltered': ?instance.licenceFiltered,
    };
