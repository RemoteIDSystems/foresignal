// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'anomaly_observation_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnomalyObservationPageCWProxy {
  AnomalyObservationPage items(List<AnomalyObservation> items);

  AnomalyObservationPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyObservationPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyObservationPage(...).copyWith(id: 12, name: "My name")
  /// ```
  AnomalyObservationPage call({
    List<AnomalyObservation> items,
    String? nextCursor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnomalyObservationPage.copyWith(...)` or call `instanceOfAnomalyObservationPage.copyWith.fieldName(value)` for a single field.
class _$AnomalyObservationPageCWProxyImpl
    implements _$AnomalyObservationPageCWProxy {
  const _$AnomalyObservationPageCWProxyImpl(this._value);

  final AnomalyObservationPage _value;

  @override
  AnomalyObservationPage items(List<AnomalyObservation> items) =>
      call(items: items);

  @override
  AnomalyObservationPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyObservationPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyObservationPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnomalyObservationPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return AnomalyObservationPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<AnomalyObservation>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $AnomalyObservationPageCopyWith on AnomalyObservationPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnomalyObservationPage.copyWith(...)` or `instanceOfAnomalyObservationPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnomalyObservationPageCWProxy get copyWith =>
      _$AnomalyObservationPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnomalyObservationPage _$AnomalyObservationPageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnomalyObservationPage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items']);
  final val = AnomalyObservationPage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => AnomalyObservation.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$AnomalyObservationPageToJson(
  AnomalyObservationPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
