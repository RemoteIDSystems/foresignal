// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_question_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastQuestionPageCWProxy {
  ForecastQuestionPage items(List<ForecastQuestionSummary> items);

  ForecastQuestionPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastQuestionPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastQuestionPage(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastQuestionPage call({
    List<ForecastQuestionSummary> items,
    String? nextCursor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastQuestionPage.copyWith(...)` or call `instanceOfForecastQuestionPage.copyWith.fieldName(value)` for a single field.
class _$ForecastQuestionPageCWProxyImpl
    implements _$ForecastQuestionPageCWProxy {
  const _$ForecastQuestionPageCWProxyImpl(this._value);

  final ForecastQuestionPage _value;

  @override
  ForecastQuestionPage items(List<ForecastQuestionSummary> items) =>
      call(items: items);

  @override
  ForecastQuestionPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastQuestionPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastQuestionPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastQuestionPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ForecastQuestionPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ForecastQuestionSummary>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ForecastQuestionPageCopyWith on ForecastQuestionPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastQuestionPage.copyWith(...)` or `instanceOfForecastQuestionPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastQuestionPageCWProxy get copyWith =>
      _$ForecastQuestionPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastQuestionPage _$ForecastQuestionPageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastQuestionPage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items']);
  final val = ForecastQuestionPage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ForecastQuestionSummary.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ForecastQuestionPageToJson(
  ForecastQuestionPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
