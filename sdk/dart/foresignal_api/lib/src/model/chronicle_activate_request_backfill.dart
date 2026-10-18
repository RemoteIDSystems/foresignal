//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_activate_request_backfill.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleActivateRequestBackfill {
  /// Returns a new [ChronicleActivateRequestBackfill] instance.
  ChronicleActivateRequestBackfill({

    required  this.from,
  });

  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final DateTime from;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleActivateRequestBackfill &&
      other.from == from;

    @override
    int get hashCode =>
        from.hashCode;

  factory ChronicleActivateRequestBackfill.fromJson(Map<String, dynamic> json) => _$ChronicleActivateRequestBackfillFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleActivateRequestBackfillToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

