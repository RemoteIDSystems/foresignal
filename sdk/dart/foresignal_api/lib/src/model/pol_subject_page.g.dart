// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pol_subject_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PolSubjectPageCWProxy {
  PolSubjectPage items(List<PolSubject> items);

  PolSubjectPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolSubjectPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolSubjectPage(...).copyWith(id: 12, name: "My name")
  /// ```
  PolSubjectPage call({List<PolSubject> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPolSubjectPage.copyWith(...)` or call `instanceOfPolSubjectPage.copyWith.fieldName(value)` for a single field.
class _$PolSubjectPageCWProxyImpl implements _$PolSubjectPageCWProxy {
  const _$PolSubjectPageCWProxyImpl(this._value);

  final PolSubjectPage _value;

  @override
  PolSubjectPage items(List<PolSubject> items) => call(items: items);

  @override
  PolSubjectPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolSubjectPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolSubjectPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PolSubjectPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return PolSubjectPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<PolSubject>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $PolSubjectPageCopyWith on PolSubjectPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPolSubjectPage.copyWith(...)` or `instanceOfPolSubjectPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PolSubjectPageCWProxy get copyWith => _$PolSubjectPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PolSubjectPage _$PolSubjectPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PolSubjectPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = PolSubjectPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => PolSubject.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PolSubjectPageToJson(PolSubjectPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
