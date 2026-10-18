// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'federation_change_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FederationChangePageCWProxy {
  FederationChangePage items(List<FederationItem> items);

  FederationChangePage lastSeq(int lastSeq);

  FederationChangePage hasMore(bool hasMore);

  FederationChangePage watermarkSeq(int? watermarkSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FederationChangePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FederationChangePage(...).copyWith(id: 12, name: "My name")
  /// ```
  FederationChangePage call({
    List<FederationItem> items,
    int lastSeq,
    bool hasMore,
    int? watermarkSeq,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFederationChangePage.copyWith(...)` or call `instanceOfFederationChangePage.copyWith.fieldName(value)` for a single field.
class _$FederationChangePageCWProxyImpl
    implements _$FederationChangePageCWProxy {
  const _$FederationChangePageCWProxyImpl(this._value);

  final FederationChangePage _value;

  @override
  FederationChangePage items(List<FederationItem> items) => call(items: items);

  @override
  FederationChangePage lastSeq(int lastSeq) => call(lastSeq: lastSeq);

  @override
  FederationChangePage hasMore(bool hasMore) => call(hasMore: hasMore);

  @override
  FederationChangePage watermarkSeq(int? watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FederationChangePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FederationChangePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FederationChangePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? lastSeq = const $CopyWithPlaceholder(),
    Object? hasMore = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
  }) {
    return FederationChangePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<FederationItem>,
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
    );
  }
}

extension $FederationChangePageCopyWith on FederationChangePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFederationChangePage.copyWith(...)` or `instanceOfFederationChangePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FederationChangePageCWProxy get copyWith =>
      _$FederationChangePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FederationChangePage _$FederationChangePageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('FederationChangePage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items', 'lastSeq', 'hasMore']);
  final val = FederationChangePage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => FederationItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    lastSeq: $checkedConvert('lastSeq', (v) => (v as num).toInt()),
    hasMore: $checkedConvert('hasMore', (v) => v as bool),
    watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$FederationChangePageToJson(
  FederationChangePage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'lastSeq': instance.lastSeq,
  'hasMore': instance.hasMore,
  'watermarkSeq': ?instance.watermarkSeq,
};
