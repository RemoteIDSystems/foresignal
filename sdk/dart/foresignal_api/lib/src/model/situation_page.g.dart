// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationPageCWProxy {
  SituationPage items(List<SituationSummary> items);

  SituationPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationPage(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationPage call({List<SituationSummary> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationPage.copyWith(...)` or call `instanceOfSituationPage.copyWith.fieldName(value)` for a single field.
class _$SituationPageCWProxyImpl implements _$SituationPageCWProxy {
  const _$SituationPageCWProxyImpl(this._value);

  final SituationPage _value;

  @override
  SituationPage items(List<SituationSummary> items) => call(items: items);

  @override
  SituationPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return SituationPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SituationSummary>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $SituationPageCopyWith on SituationPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationPage.copyWith(...)` or `instanceOfSituationPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationPageCWProxy get copyWith => _$SituationPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationPage _$SituationPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SituationPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = SituationPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => SituationSummary.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SituationPageToJson(SituationPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
