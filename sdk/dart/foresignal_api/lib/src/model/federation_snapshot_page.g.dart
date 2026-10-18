// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'federation_snapshot_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FederationSnapshotPageCWProxy {
  FederationSnapshotPage items(List<FederationItem> items);

  FederationSnapshotPage atSeq(int atSeq);

  FederationSnapshotPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FederationSnapshotPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FederationSnapshotPage(...).copyWith(id: 12, name: "My name")
  /// ```
  FederationSnapshotPage call({
    List<FederationItem> items,
    int atSeq,
    String? nextCursor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFederationSnapshotPage.copyWith(...)` or call `instanceOfFederationSnapshotPage.copyWith.fieldName(value)` for a single field.
class _$FederationSnapshotPageCWProxyImpl
    implements _$FederationSnapshotPageCWProxy {
  const _$FederationSnapshotPageCWProxyImpl(this._value);

  final FederationSnapshotPage _value;

  @override
  FederationSnapshotPage items(List<FederationItem> items) =>
      call(items: items);

  @override
  FederationSnapshotPage atSeq(int atSeq) => call(atSeq: atSeq);

  @override
  FederationSnapshotPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FederationSnapshotPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FederationSnapshotPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FederationSnapshotPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? atSeq = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return FederationSnapshotPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<FederationItem>,
      atSeq: atSeq == const $CopyWithPlaceholder() || atSeq == null
          ? _value.atSeq
          // ignore: cast_nullable_to_non_nullable
          : atSeq as int,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $FederationSnapshotPageCopyWith on FederationSnapshotPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFederationSnapshotPage.copyWith(...)` or `instanceOfFederationSnapshotPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FederationSnapshotPageCWProxy get copyWith =>
      _$FederationSnapshotPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FederationSnapshotPage _$FederationSnapshotPageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('FederationSnapshotPage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items', 'atSeq']);
  final val = FederationSnapshotPage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => FederationItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    atSeq: $checkedConvert('atSeq', (v) => (v as num).toInt()),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$FederationSnapshotPageToJson(
  FederationSnapshotPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'atSeq': instance.atSeq,
  'nextCursor': ?instance.nextCursor,
};
