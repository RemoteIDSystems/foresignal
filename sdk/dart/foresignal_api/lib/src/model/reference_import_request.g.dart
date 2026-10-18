// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'reference_import_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReferenceImportRequestCWProxy {
  ReferenceImportRequest dataset(ReferenceImportRequestDatasetEnum dataset);

  ReferenceImportRequest params(ReferenceImportRequestParams? params);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceImportRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceImportRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ReferenceImportRequest call({
    ReferenceImportRequestDatasetEnum dataset,
    ReferenceImportRequestParams? params,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReferenceImportRequest.copyWith(...)` or call `instanceOfReferenceImportRequest.copyWith.fieldName(value)` for a single field.
class _$ReferenceImportRequestCWProxyImpl
    implements _$ReferenceImportRequestCWProxy {
  const _$ReferenceImportRequestCWProxyImpl(this._value);

  final ReferenceImportRequest _value;

  @override
  ReferenceImportRequest dataset(ReferenceImportRequestDatasetEnum dataset) =>
      call(dataset: dataset);

  @override
  ReferenceImportRequest params(ReferenceImportRequestParams? params) =>
      call(params: params);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceImportRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceImportRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReferenceImportRequest call({
    Object? dataset = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
  }) {
    return ReferenceImportRequest(
      dataset: dataset == const $CopyWithPlaceholder() || dataset == null
          ? _value.dataset
          // ignore: cast_nullable_to_non_nullable
          : dataset as ReferenceImportRequestDatasetEnum,
      params: params == const $CopyWithPlaceholder()
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as ReferenceImportRequestParams?,
    );
  }
}

extension $ReferenceImportRequestCopyWith on ReferenceImportRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReferenceImportRequest.copyWith(...)` or `instanceOfReferenceImportRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReferenceImportRequestCWProxy get copyWith =>
      _$ReferenceImportRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReferenceImportRequest _$ReferenceImportRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReferenceImportRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['dataset']);
  final val = ReferenceImportRequest(
    dataset: $checkedConvert(
      'dataset',
      (v) => $enumDecode(_$ReferenceImportRequestDatasetEnumEnumMap, v),
    ),
    params: $checkedConvert(
      'params',
      (v) => v == null
          ? null
          : ReferenceImportRequestParams.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$ReferenceImportRequestToJson(
  ReferenceImportRequest instance,
) => <String, dynamic>{
  'dataset': _$ReferenceImportRequestDatasetEnumEnumMap[instance.dataset]!,
  'params': ?instance.params?.toJson(),
};

const _$ReferenceImportRequestDatasetEnumEnumMap = {
  ReferenceImportRequestDatasetEnum.ucdpGed: 'ucdp-ged',
  ReferenceImportRequestDatasetEnum.acled: 'acled',
  ReferenceImportRequestDatasetEnum.usgsComcat: 'usgs-comcat',
  ReferenceImportRequestDatasetEnum.gdacs: 'gdacs',
};
