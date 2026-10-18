// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cpa_batch_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CpaBatchRequestCWProxy {
  CpaBatchRequest subject(String subject);

  CpaBatchRequest filter(CpaBatchRequestFilter filter);

  CpaBatchRequest horizon(String? horizon);

  CpaBatchRequest maxDcpaM(num? maxDcpaM);

  CpaBatchRequest top(int? top);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CpaBatchRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CpaBatchRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  CpaBatchRequest call({
    String subject,
    CpaBatchRequestFilter filter,
    String? horizon,
    num? maxDcpaM,
    int? top,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCpaBatchRequest.copyWith(...)` or call `instanceOfCpaBatchRequest.copyWith.fieldName(value)` for a single field.
class _$CpaBatchRequestCWProxyImpl implements _$CpaBatchRequestCWProxy {
  const _$CpaBatchRequestCWProxyImpl(this._value);

  final CpaBatchRequest _value;

  @override
  CpaBatchRequest subject(String subject) => call(subject: subject);

  @override
  CpaBatchRequest filter(CpaBatchRequestFilter filter) => call(filter: filter);

  @override
  CpaBatchRequest horizon(String? horizon) => call(horizon: horizon);

  @override
  CpaBatchRequest maxDcpaM(num? maxDcpaM) => call(maxDcpaM: maxDcpaM);

  @override
  CpaBatchRequest top(int? top) => call(top: top);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CpaBatchRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CpaBatchRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CpaBatchRequest call({
    Object? subject = const $CopyWithPlaceholder(),
    Object? filter = const $CopyWithPlaceholder(),
    Object? horizon = const $CopyWithPlaceholder(),
    Object? maxDcpaM = const $CopyWithPlaceholder(),
    Object? top = const $CopyWithPlaceholder(),
  }) {
    return CpaBatchRequest(
      subject: subject == const $CopyWithPlaceholder() || subject == null
          ? _value.subject
          // ignore: cast_nullable_to_non_nullable
          : subject as String,
      filter: filter == const $CopyWithPlaceholder() || filter == null
          ? _value.filter
          // ignore: cast_nullable_to_non_nullable
          : filter as CpaBatchRequestFilter,
      horizon: horizon == const $CopyWithPlaceholder()
          ? _value.horizon
          // ignore: cast_nullable_to_non_nullable
          : horizon as String?,
      maxDcpaM: maxDcpaM == const $CopyWithPlaceholder()
          ? _value.maxDcpaM
          // ignore: cast_nullable_to_non_nullable
          : maxDcpaM as num?,
      top: top == const $CopyWithPlaceholder()
          ? _value.top
          // ignore: cast_nullable_to_non_nullable
          : top as int?,
    );
  }
}

extension $CpaBatchRequestCopyWith on CpaBatchRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCpaBatchRequest.copyWith(...)` or `instanceOfCpaBatchRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CpaBatchRequestCWProxy get copyWith => _$CpaBatchRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CpaBatchRequest _$CpaBatchRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CpaBatchRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['subject', 'filter']);
      final val = CpaBatchRequest(
        subject: $checkedConvert('subject', (v) => v as String),
        filter: $checkedConvert(
          'filter',
          (v) => CpaBatchRequestFilter.fromJson(v as Map<String, dynamic>),
        ),
        horizon: $checkedConvert('horizon', (v) => v as String? ?? '2h'),
        maxDcpaM: $checkedConvert('maxDcpaM', (v) => v as num?),
        top: $checkedConvert('top', (v) => (v as num?)?.toInt() ?? 20),
      );
      return val;
    });

Map<String, dynamic> _$CpaBatchRequestToJson(CpaBatchRequest instance) =>
    <String, dynamic>{
      'subject': instance.subject,
      'filter': instance.filter.toJson(),
      'horizon': ?instance.horizon,
      'maxDcpaM': ?instance.maxDcpaM,
      'top': ?instance.top,
    };
