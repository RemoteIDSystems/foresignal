// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_side.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorSideCWProxy {
  PrecursorSide selector(EventSelector? selector);

  PrecursorSide seriesRef(String? seriesRef);

  PrecursorSide family(String? family);

  PrecursorSide label(String label);

  PrecursorSide threshold(num? threshold);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorSide(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorSide(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorSide call({
    EventSelector? selector,
    String? seriesRef,
    String? family,
    String label,
    num? threshold,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorSide.copyWith(...)` or call `instanceOfPrecursorSide.copyWith.fieldName(value)` for a single field.
class _$PrecursorSideCWProxyImpl implements _$PrecursorSideCWProxy {
  const _$PrecursorSideCWProxyImpl(this._value);

  final PrecursorSide _value;

  @override
  PrecursorSide selector(EventSelector? selector) => call(selector: selector);

  @override
  PrecursorSide seriesRef(String? seriesRef) => call(seriesRef: seriesRef);

  @override
  PrecursorSide family(String? family) => call(family: family);

  @override
  PrecursorSide label(String label) => call(label: label);

  @override
  PrecursorSide threshold(num? threshold) => call(threshold: threshold);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorSide(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorSide(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorSide call({
    Object? selector = const $CopyWithPlaceholder(),
    Object? seriesRef = const $CopyWithPlaceholder(),
    Object? family = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? threshold = const $CopyWithPlaceholder(),
  }) {
    return PrecursorSide(
      selector: selector == const $CopyWithPlaceholder()
          ? _value.selector
          // ignore: cast_nullable_to_non_nullable
          : selector as EventSelector?,
      seriesRef: seriesRef == const $CopyWithPlaceholder()
          ? _value.seriesRef
          // ignore: cast_nullable_to_non_nullable
          : seriesRef as String?,
      family: family == const $CopyWithPlaceholder()
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String?,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String,
      threshold: threshold == const $CopyWithPlaceholder()
          ? _value.threshold
          // ignore: cast_nullable_to_non_nullable
          : threshold as num?,
    );
  }
}

extension $PrecursorSideCopyWith on PrecursorSide {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorSide.copyWith(...)` or `instanceOfPrecursorSide.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorSideCWProxy get copyWith => _$PrecursorSideCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorSide _$PrecursorSideFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PrecursorSide', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['label']);
      final val = PrecursorSide(
        selector: $checkedConvert(
          'selector',
          (v) => v == null
              ? null
              : EventSelector.fromJson(v as Map<String, dynamic>),
        ),
        seriesRef: $checkedConvert('seriesRef', (v) => v as String?),
        family: $checkedConvert('family', (v) => v as String?),
        label: $checkedConvert('label', (v) => v as String),
        threshold: $checkedConvert('threshold', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$PrecursorSideToJson(PrecursorSide instance) =>
    <String, dynamic>{
      'selector': ?instance.selector?.toJson(),
      'seriesRef': ?instance.seriesRef,
      'family': ?instance.family,
      'label': instance.label,
      'threshold': ?instance.threshold,
    };
