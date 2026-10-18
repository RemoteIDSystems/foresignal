//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/active_window_windows_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'active_window.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ActiveWindow {
  /// Returns a new [ActiveWindow] instance.
  ActiveWindow({

    required  this.timezone,

    required  this.windows,
  });

      /// IANA timezone
  @JsonKey(
    
    name: r'timezone',
    required: true,
    includeIfNull: false,
  )


  final String timezone;



  @JsonKey(
    
    name: r'windows',
    required: true,
    includeIfNull: false,
  )


  final List<ActiveWindowWindowsInner> windows;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ActiveWindow &&
      other.timezone == timezone &&
      other.windows == windows;

    @override
    int get hashCode =>
        timezone.hashCode +
        windows.hashCode;

  factory ActiveWindow.fromJson(Map<String, dynamic> json) => _$ActiveWindowFromJson(json);

  Map<String, dynamic> toJson() => _$ActiveWindowToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

