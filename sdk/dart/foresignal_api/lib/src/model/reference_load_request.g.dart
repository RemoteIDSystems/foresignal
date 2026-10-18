// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'reference_load_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReferenceLoadRequestCWProxy {
  ReferenceLoadRequest datasets(List<String> datasets);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceLoadRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceLoadRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ReferenceLoadRequest call({List<String> datasets});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReferenceLoadRequest.copyWith(...)` or call `instanceOfReferenceLoadRequest.copyWith.fieldName(value)` for a single field.
class _$ReferenceLoadRequestCWProxyImpl
    implements _$ReferenceLoadRequestCWProxy {
  const _$ReferenceLoadRequestCWProxyImpl(this._value);

  final ReferenceLoadRequest _value;

  @override
  ReferenceLoadRequest datasets(List<String> datasets) =>
      call(datasets: datasets);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceLoadRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceLoadRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReferenceLoadRequest call({Object? datasets = const $CopyWithPlaceholder()}) {
    return ReferenceLoadRequest(
      datasets: datasets == const $CopyWithPlaceholder() || datasets == null
          ? _value.datasets
          // ignore: cast_nullable_to_non_nullable
          : datasets as List<String>,
    );
  }
}

extension $ReferenceLoadRequestCopyWith on ReferenceLoadRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReferenceLoadRequest.copyWith(...)` or `instanceOfReferenceLoadRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReferenceLoadRequestCWProxy get copyWith =>
      _$ReferenceLoadRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReferenceLoadRequest _$ReferenceLoadRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReferenceLoadRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['datasets']);
  final val = ReferenceLoadRequest(
    datasets: $checkedConvert(
      'datasets',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ReferenceLoadRequestToJson(
  ReferenceLoadRequest instance,
) => <String, dynamic>{'datasets': instance.datasets};
