//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cot_backfill_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CotBackfillRequest {
  /// Returns a new [CotBackfillRequest] instance.
  CotBackfillRequest({

     this.from,
  });

      /// Oldest report date to fetch
  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CotBackfillRequest &&
      other.from == from;

    @override
    int get hashCode =>
        from.hashCode;

  factory CotBackfillRequest.fromJson(Map<String, dynamic> json) => _$CotBackfillRequestFromJson(json);

  Map<String, dynamic> toJson() => _$CotBackfillRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

