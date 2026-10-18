// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'history_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HistoryPageCWProxy {
  HistoryPage items(List<RecordVersion> items);

  HistoryPage nextCursor(String? nextCursor);

  HistoryPage attributions(List<Attribution>? attributions);

  HistoryPage licenceFiltered(int? licenceFiltered);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HistoryPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HistoryPage(...).copyWith(id: 12, name: "My name")
  /// ```
  HistoryPage call({
    List<RecordVersion> items,
    String? nextCursor,
    List<Attribution>? attributions,
    int? licenceFiltered,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHistoryPage.copyWith(...)` or call `instanceOfHistoryPage.copyWith.fieldName(value)` for a single field.
class _$HistoryPageCWProxyImpl implements _$HistoryPageCWProxy {
  const _$HistoryPageCWProxyImpl(this._value);

  final HistoryPage _value;

  @override
  HistoryPage items(List<RecordVersion> items) => call(items: items);

  @override
  HistoryPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  @override
  HistoryPage attributions(List<Attribution>? attributions) =>
      call(attributions: attributions);

  @override
  HistoryPage licenceFiltered(int? licenceFiltered) =>
      call(licenceFiltered: licenceFiltered);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HistoryPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HistoryPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HistoryPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
    Object? attributions = const $CopyWithPlaceholder(),
    Object? licenceFiltered = const $CopyWithPlaceholder(),
  }) {
    return HistoryPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<RecordVersion>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
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

extension $HistoryPageCopyWith on HistoryPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHistoryPage.copyWith(...)` or `instanceOfHistoryPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HistoryPageCWProxy get copyWith => _$HistoryPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HistoryPage _$HistoryPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('HistoryPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = HistoryPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => RecordVersion.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
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

Map<String, dynamic> _$HistoryPageToJson(HistoryPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
      'attributions': ?instance.attributions?.map((e) => e.toJson()).toList(),
      'licenceFiltered': ?instance.licenceFiltered,
    };
